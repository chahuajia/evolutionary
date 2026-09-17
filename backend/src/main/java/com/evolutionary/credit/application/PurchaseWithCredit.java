package com.evolutionary.credit.application;

import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.application.OrderRepository;
import com.evolutionary.commerce.application.ProductRepository;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.LedgerRefType;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.credit.domain.CreditErrorCode;
import com.evolutionary.credit.domain.CreditLedgerDebt;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditProfile;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * 信用先用后付购买（AC-48 / AC-49 · INV-17）。
 *
 * <p>Order PAID 但不写 ORDER_PAYMENT_BALANCE；仅写 {@link CreditLedgerDebt} 并创建 Entitlement。
 */
public final class PurchaseWithCredit {

    private final ProductRepository products;
    private final OrderRepository orders;
    private final EntitlementRepository entitlements;
    private final LedgerRepository ledger;
    private final CreditProfileRepository profiles;
    private final CreditLedgerDebtRepository debts;
    private final Clock clock;

    public PurchaseWithCredit(
            ProductRepository products,
            OrderRepository orders,
            EntitlementRepository entitlements,
            LedgerRepository ledger,
            CreditProfileRepository profiles,
            CreditLedgerDebtRepository debts,
            Clock clock) {
        this.products = Objects.requireNonNull(products, "products");
        this.orders = Objects.requireNonNull(orders, "orders");
        this.entitlements = Objects.requireNonNull(entitlements, "entitlements");
        this.ledger = Objects.requireNonNull(ledger, "ledger");
        this.profiles = Objects.requireNonNull(profiles, "profiles");
        this.debts = Objects.requireNonNull(debts, "debts");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public CreditOutcome<CreditPurchaseResult> execute(String userId, String productId) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(productId, "productId");

        Product product = products.get(productId);
        if (!product.isPublished()) {
            return CreditOutcome.err(CreditErrorCode.PRODUCT_NOT_PUBLISHED, "商品未上架");
        }
        if (product.isMetered()) {
            return CreditOutcome.err(
                    CreditErrorCode.PRODUCT_NOT_ELIGIBLE, "计量商品不适用信用购");
        }

        CreditProfile profile = profiles.get(userId);
        CreditOutcome<CreditProfile> charged = profile.charge(product.price());
        if (charged instanceof CreditOutcome.Err<CreditProfile> err) {
            return CreditOutcome.err(err.code(), err.message());
        }
        CreditProfile after = ((CreditOutcome.Ok<CreditProfile>) charged).value();

        var now = clock.instant();
        Order created =
                Order.create(
                        newId("ord"),
                        userId,
                        product.id(),
                        product.orgId(),
                        product.price(),
                        now);
        Order paid = created.pay(now);

        CreditLedgerDebt debt =
                CreditLedgerDebt.open(
                        newId("debt"), userId, paid.id(), product.price(), now);
        Entitlement entitlement = Entitlement.createActive(newId("ent"), paid, product, now);

        profiles.save(after);
        debts.save(debt);
        orders.save(paid);
        entitlements.save(entitlement);

        // INV-17：信用购不得产生订单余额支付分录
        boolean hasBalancePayment =
                ledger.findByOrderId(paid.id()).stream()
                        .anyMatch(e -> e.refType() == LedgerRefType.ORDER_PAYMENT_BALANCE);
        if (hasBalancePayment) {
            throw new IllegalStateException("INV-17 违规：信用购不得写入 ORDER_PAYMENT_BALANCE");
        }

        return CreditOutcome.ok(new CreditPurchaseResult(paid, entitlement, debt, after));
    }

    private static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
