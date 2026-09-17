package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerInvariant;
import com.evolutionary.commerce.domain.LedgerRefType;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.commerce.domain.OrderStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 订单退款。
 *
 * <p>phase-2（AC-20 / INV-9）：查该订单支付分录，先退 BALANCE 再退 POINTS；
 * 各类型退款金额与支付分录相等；权益 REVOKED。
 */
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
            return DomainOutcome.err(DomainErrorCode.ORDER_NOT_REFUNDABLE, "订单不是 PAID 状态");
        }

        Entitlement entitlement =
                entitlements
                        .findByOrderId(orderId)
                        .orElseThrow(() -> new IllegalStateException("订单缺少权益"));

        if (usages.findStartedByEntitlement(entitlement.id()).isPresent()) {
            return DomainOutcome.err(
                    DomainErrorCode.REFUND_BLOCKED_IN_PROGRESS_SWAP,
                    "换电进行中，禁止退款");
        }

        List<LedgerEntry> payments =
                ledger.findByOrderId(orderId).stream().filter(RefundOrder::isPaymentEntry).toList();
        if (payments.isEmpty()) {
            throw new IllegalStateException("订单缺少支付分录");
        }

        Instant now = clock.instant();
        Account orgSettlement =
                accounts.findOrgSettlement(order.orgId(), order.paidAmount().currency());
        Account updatedOrg = orgSettlement;

        // AC-20：先退余额，再退积分；INV-9：金额逐类型相等
        for (LedgerEntry payment : balancePayments(payments)) {
            updatedOrg = refundBalanceEntry(payment, orgSettlement.id(), updatedOrg, order.id(), now);
        }
        for (LedgerEntry payment : pointsPayments(payments)) {
            updatedOrg = refundPointsEntry(payment, orgSettlement.id(), updatedOrg, order.id(), now);
        }

        accounts.save(updatedOrg);
        LedgerInvariant.assertBalanced(ledger.findAll());

        Entitlement revoked = entitlement.revoke();
        Order refunded = order.refund(now);
        entitlements.save(revoked);
        orders.save(refunded);

        return DomainOutcome.ok(new RefundResult(refunded, revoked));
    }

    private Account refundBalanceEntry(
            LedgerEntry payment,
            String orgSettlementId,
            Account orgSettlement,
            String orderId,
            Instant now) {
        Account userAccount = accounts.get(payment.debitAccountId());
        Account creditedUser = userAccount.credit(payment.amount().cents());
        Account debitedOrg = orgSettlement.debit(payment.amount().cents());
        ledger.append(
                LedgerEntry.orderRefundBalance(
                        newId("led"),
                        orgSettlementId,
                        userAccount.id(),
                        payment.amount(),
                        orderId,
                        now));
        accounts.save(creditedUser);
        return debitedOrg;
    }

    private Account refundPointsEntry(
            LedgerEntry payment,
            String orgSettlementId,
            Account orgSettlement,
            String orderId,
            Instant now) {
        Account userPoints = accounts.get(payment.debitAccountId());
        Account creditedPoints = userPoints.credit(payment.amount().cents());
        Account debitedOrg = orgSettlement.debit(payment.amount().cents());
        ledger.append(
                LedgerEntry.orderRefundPoints(
                        newId("led"),
                        orgSettlementId,
                        userPoints.id(),
                        payment.amount(),
                        orderId,
                        now));
        accounts.save(creditedPoints);
        return debitedOrg;
    }

    private static boolean isPaymentEntry(LedgerEntry entry) {
        return switch (entry.refType()) {
            case ORDER_PAYMENT, ORDER_PAYMENT_BALANCE, ORDER_PAYMENT_POINTS -> true;
            default -> false;
        };
    }

    private static List<LedgerEntry> balancePayments(List<LedgerEntry> payments) {
        List<LedgerEntry> result = new ArrayList<>();
        for (LedgerEntry payment : payments) {
            if (payment.refType() == LedgerRefType.ORDER_PAYMENT
                    || payment.refType() == LedgerRefType.ORDER_PAYMENT_BALANCE) {
                result.add(payment);
            }
        }
        return result;
    }

    private static List<LedgerEntry> pointsPayments(List<LedgerEntry> payments) {
        return payments.stream()
                .filter(e -> e.refType() == LedgerRefType.ORDER_PAYMENT_POINTS)
                .toList();
    }

    private static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
