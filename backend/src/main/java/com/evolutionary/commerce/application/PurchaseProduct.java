package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerInvariant;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.commerce.domain.PaymentIntent;
import com.evolutionary.commerce.domain.Product;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 购买商品。
 *
 * <p>phase-2：支持 {@link PaymentIntent} 混合支付；默认入口仍为纯余额（向后兼容）。
 */
public final class PurchaseProduct {

    private final ProductRepository products;
    private final AccountRepository accounts;
    private final OrderRepository orders;
    private final EntitlementRepository entitlements;
    private final LedgerRepository ledger;
    private final Clock clock;

    public PurchaseProduct(
            ProductRepository products,
            AccountRepository accounts,
            OrderRepository orders,
            EntitlementRepository entitlements,
            LedgerRepository ledger,
            Clock clock) {
        this.products = Objects.requireNonNull(products, "products");
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.orders = Objects.requireNonNull(orders, "orders");
        this.entitlements = Objects.requireNonNull(entitlements, "entitlements");
        this.ledger = Objects.requireNonNull(ledger, "ledger");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** 纯余额购买（AC-17 回归）。 */
    public DomainOutcome<PurchaseResult> execute(String userId, String productId) {
        Product product = products.get(productId);
        return execute(userId, productId, PaymentIntent.balanceOnly(product.price()));
    }

    public DomainOutcome<PurchaseResult> execute(
            String userId, String productId, PaymentIntent intent) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(productId, "productId");
        Objects.requireNonNull(intent, "intent");

        Product product = products.get(productId);
        if (!product.isPublished()) {
            return DomainOutcome.err(DomainErrorCode.PRODUCT_NOT_PUBLISHED, "商品未上架");
        }
        if (!intent.matchesPrice(product.price())) {
            return DomainOutcome.err(
                    DomainErrorCode.INSUFFICIENT_BALANCE, "支付意图金额必须等于标价");
        }

        var now = clock.instant();
        Account orgSettlement =
                accounts.findOrgSettlement(product.orgId(), product.price().currency());

        Account userPoints = null;
        Account userBalance = null;
        long pointsPart = intent.usePointsCents();
        long balancePart = intent.useBalanceCents();

        if (pointsPart > 0) {
            userPoints = accounts.findUserPoints(userId, product.price().currency());
            if (!userPoints.pointsUsableAt(now)) {
                return DomainOutcome.err(DomainErrorCode.POINTS_EXPIRED, "积分已过期");
            }
            if (!userPoints.canCoverCents(pointsPart)) {
                return DomainOutcome.err(DomainErrorCode.INSUFFICIENT_POINTS, "积分不足");
            }
        }
        if (balancePart > 0) {
            userBalance = accounts.findUserBalance(userId, product.price().currency());
            if (!userBalance.canCoverCents(balancePart)) {
                return DomainOutcome.err(DomainErrorCode.INSUFFICIENT_BALANCE, "余额不足");
            }
        }

        Order created =
                Order.create(
                        newId("ord"),
                        userId,
                        product.id(),
                        product.orgId(),
                        product.price(),
                        now);

        Account updatedOrg = orgSettlement;
        if (pointsPart > 0) {
            Money pointsMoney = Money.cny(pointsPart);
            Account debitedPoints = userPoints.debit(pointsPart);
            updatedOrg = updatedOrg.credit(pointsPart);
            ledger.append(
                    LedgerEntry.orderPaymentPoints(
                            newId("led"),
                            userPoints.id(),
                            orgSettlement.id(),
                            pointsMoney,
                            created.id(),
                            now));
            accounts.save(debitedPoints);
        }
        if (balancePart > 0) {
            Money balanceMoney = Money.cny(balancePart);
            Account debitedBalance = userBalance.debit(balancePart);
            updatedOrg = updatedOrg.credit(balancePart);
            ledger.append(
                    LedgerEntry.orderPaymentBalance(
                            newId("led"),
                            userBalance.id(),
                            orgSettlement.id(),
                            balanceMoney,
                            created.id(),
                            now));
            accounts.save(debitedBalance);
        }

        accounts.save(updatedOrg);
        LedgerInvariant.assertBalanced(ledger.findAll());

        Order paid = created.pay(now);
        Entitlement entitlement = Entitlement.createActive(newId("ent"), paid, product, now);
        orders.save(paid);
        entitlements.save(entitlement);

        return DomainOutcome.ok(new PurchaseResult(paid, entitlement));
    }

    private static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
