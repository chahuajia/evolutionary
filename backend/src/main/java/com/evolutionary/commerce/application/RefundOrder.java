package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerInvariant;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.commerce.domain.OrderStatus;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class RefundOrder {

    private final OrderRepository orders;
    private final EntitlementRepository entitlements;
    private final UsageEventRepository usages;
    private final AccountRepository accounts;
    private final LedgerRepository ledger;
    private final Clock clock;

    public RefundOrder(
            OrderRepository orders,
            EntitlementRepository entitlements,
            UsageEventRepository usages,
            AccountRepository accounts,
            LedgerRepository ledger,
            Clock clock) {
        this.orders = Objects.requireNonNull(orders, "orders");
        this.entitlements = Objects.requireNonNull(entitlements, "entitlements");
        this.usages = Objects.requireNonNull(usages, "usages");
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.ledger = Objects.requireNonNull(ledger, "ledger");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public DomainOutcome<RefundResult> execute(String orderId) {
        Objects.requireNonNull(orderId, "orderId");

        Order order = orders.get(orderId);
        if (order.status() != OrderStatus.PAID) {
            return DomainOutcome.err(DomainErrorCode.ORDER_NOT_REFUNDABLE, "order is not PAID");
        }

        Entitlement entitlement =
                entitlements
                        .findByOrderId(orderId)
                        .orElseThrow(() -> new IllegalStateException("missing entitlement for order"));

        if (usages.findStartedByEntitlement(entitlement.id()).isPresent()) {
            return DomainOutcome.err(
                    DomainErrorCode.REFUND_BLOCKED_IN_PROGRESS_SWAP,
                    "refund blocked: swap in progress");
        }

        var now = clock.instant();
        Account userBalance =
                accounts.findUserBalance(order.userId(), order.paidAmount().currency());
        Account orgSettlement =
                accounts.findOrgSettlement(order.orgId(), order.paidAmount().currency());

        Account creditedUser = userBalance.credit(order.paidAmount().cents());
        Account debitedOrg = orgSettlement.debit(order.paidAmount().cents());

        LedgerEntry refundEntry =
                LedgerEntry.orderRefund(
                        newId("led"),
                        orgSettlement.id(),
                        userBalance.id(),
                        order.paidAmount(),
                        order.id(),
                        now);

        ledger.append(refundEntry);
        List<LedgerEntry> allEntries = ledger.findAll();
        LedgerInvariant.assertBalanced(allEntries);

        Entitlement revoked = entitlement.revoke();
        Order refunded = order.refund(now);

        accounts.save(creditedUser);
        accounts.save(debitedOrg);
        entitlements.save(revoked);
        orders.save(refunded);

        return DomainOutcome.ok(new RefundResult(refunded, revoked));
    }

    private static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
