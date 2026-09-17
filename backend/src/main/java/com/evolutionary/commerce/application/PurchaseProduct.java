package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerInvariant;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.commerce.domain.Product;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    public DomainOutcome<PurchaseResult> execute(String userId, String productId) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(productId, "productId");

        Product product = products.get(productId);
        if (!product.isPublished()) {
            return DomainOutcome.err(DomainErrorCode.PRODUCT_NOT_PUBLISHED, "product is not published");
        }

        Account userBalance = accounts.findUserBalance(userId, product.price().currency());
        if (!userBalance.canCover(product.price())) {
            return DomainOutcome.err(DomainErrorCode.INSUFFICIENT_BALANCE, "insufficient balance");
        }

        var now = clock.instant();
        Order created =
                Order.create(
                        newId("ord"),
                        userId,
                        product.id(),
                        product.orgId(),
                        product.price(),
                        now);

        Account orgSettlement =
                accounts.findOrgSettlement(product.orgId(), product.price().currency());

        Account debitedUser = userBalance.debit(product.price().cents());
        Account creditedOrg = orgSettlement.credit(product.price().cents());

        LedgerEntry payment =
                LedgerEntry.orderPayment(
                        newId("led"),
                        userBalance.id(),
                        orgSettlement.id(),
                        product.price(),
                        created.id(),
                        now);

        ledger.append(payment);
        List<LedgerEntry> allEntries = ledger.findAll();
        LedgerInvariant.assertBalanced(allEntries);

        Order paid = created.pay(now);
        Entitlement entitlement = Entitlement.createActive(newId("ent"), paid, product, now);

        accounts.save(debitedUser);
        accounts.save(creditedOrg);
        orders.save(paid);
        entitlements.save(entitlement);

        return DomainOutcome.ok(new PurchaseResult(paid, entitlement));
    }

    private static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
