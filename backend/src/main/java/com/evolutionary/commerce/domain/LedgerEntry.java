package com.evolutionary.commerce.domain;

import java.time.Instant;
import java.util.Objects;

/** 追加-only 分录；单条内部借贷相等。 */
public final class LedgerEntry {

    private final String id;
    private final String debitAccountId;
    private final String creditAccountId;
    private final Money amount;
    private final LedgerRefType refType;
    private final String refId;
    private final Instant createdAt;

    private LedgerEntry(
            String id,
            String debitAccountId,
            String creditAccountId,
            Money amount,
            LedgerRefType refType,
            String refId,
            Instant createdAt) {
        this.id = id;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.amount = amount;
        this.refType = refType;
        this.refId = refId;
        this.createdAt = createdAt;
    }

    /** 纯余额支付（AC-17）；refType = ORDER_PAYMENT_BALANCE。 */
    public static LedgerEntry orderPayment(
            String id,
            String userBalanceAccountId,
            String orgSettlementAccountId,
            Money amount,
            String orderId,
            Instant createdAt) {
        return orderPaymentBalance(
                id, userBalanceAccountId, orgSettlementAccountId, amount, orderId, createdAt);
    }

    public static LedgerEntry orderPaymentBalance(
            String id,
            String userBalanceAccountId,
            String orgSettlementAccountId,
            Money amount,
            String orderId,
            Instant createdAt) {
        return new LedgerEntry(
                requireId(id),
                requireId(userBalanceAccountId),
                requireId(orgSettlementAccountId),
                Objects.requireNonNull(amount, "amount"),
                LedgerRefType.ORDER_PAYMENT_BALANCE,
                requireId(orderId),
                Objects.requireNonNull(createdAt, "createdAt"));
    }

    public static LedgerEntry orderPaymentPoints(
            String id,
            String userPointsAccountId,
            String orgSettlementAccountId,
            Money amount,
            String orderId,
            Instant createdAt) {
        return new LedgerEntry(
                requireId(id),
                requireId(userPointsAccountId),
                requireId(orgSettlementAccountId),
                Objects.requireNonNull(amount, "amount"),
                LedgerRefType.ORDER_PAYMENT_POINTS,
                requireId(orderId),
                Objects.requireNonNull(createdAt, "createdAt"));
    }

    /** 退款反向：优先用于纯余额；phase-2 请用 orderRefundBalance。 */
    public static LedgerEntry orderRefund(
            String id,
            String orgSettlementAccountId,
            String userBalanceAccountId,
            Money amount,
            String orderId,
            Instant createdAt) {
        return orderRefundBalance(
                id, orgSettlementAccountId, userBalanceAccountId, amount, orderId, createdAt);
    }

    public static LedgerEntry orderRefundBalance(
            String id,
            String orgSettlementAccountId,
            String userBalanceAccountId,
            Money amount,
            String orderId,
            Instant createdAt) {
        return new LedgerEntry(
                requireId(id),
                requireId(orgSettlementAccountId),
                requireId(userBalanceAccountId),
                Objects.requireNonNull(amount, "amount"),
                LedgerRefType.ORDER_REFUND_BALANCE,
                requireId(orderId),
                Objects.requireNonNull(createdAt, "createdAt"));
    }

    public static LedgerEntry orderRefundPoints(
            String id,
            String orgSettlementAccountId,
            String userPointsAccountId,
            Money amount,
            String orderId,
            Instant createdAt) {
        return new LedgerEntry(
                requireId(id),
                requireId(orgSettlementAccountId),
                requireId(userPointsAccountId),
                Objects.requireNonNull(amount, "amount"),
                LedgerRefType.ORDER_REFUND_POINTS,
                requireId(orderId),
                Objects.requireNonNull(createdAt, "createdAt"));
    }

    /** INV-8：UsageEvent COMPLETED 后按电量扣款；refId = usageEventId。 */
    public static LedgerEntry meteredCharge(
            String id,
            String userBalanceAccountId,
            String orgSettlementAccountId,
            Money amount,
            String usageEventId,
            Instant createdAt) {
        return new LedgerEntry(
                requireId(id),
                requireId(userBalanceAccountId),
                requireId(orgSettlementAccountId),
                Objects.requireNonNull(amount, "amount"),
                LedgerRefType.METERED_CHARGE,
                requireId(usageEventId),
                Objects.requireNonNull(createdAt, "createdAt"));
    }

    /**
     * 分润结算：SETTLEMENT 户间划转；refId = SettlementBatchId。
     *
     * <p>借：清算户；贷：受益组织 SETTLEMENT。
     */
    public static LedgerEntry profitSharingSettlement(
            String id,
            String clearingAccountId,
            String orgSettlementAccountId,
            Money amount,
            String batchId,
            Instant createdAt) {
        return new LedgerEntry(
                requireId(id),
                requireId(clearingAccountId),
                requireId(orgSettlementAccountId),
                Objects.requireNonNull(amount, "amount"),
                LedgerRefType.PROFIT_SHARING_SETTLEMENT,
                requireId(batchId),
                Objects.requireNonNull(createdAt, "createdAt"));
    }

    /**
     * 信用账单还款：借用户 BALANCE，贷清账过渡户；refId = BillingStatementId。
     */
    public static LedgerEntry creditStatementRepayment(
            String id,
            String userBalanceAccountId,
            String clearingAccountId,
            Money amount,
            String statementId,
            Instant createdAt) {
        return new LedgerEntry(
                requireId(id),
                requireId(userBalanceAccountId),
                requireId(clearingAccountId),
                Objects.requireNonNull(amount, "amount"),
                LedgerRefType.CREDIT_STATEMENT_REPAYMENT,
                requireId(statementId),
                Objects.requireNonNull(createdAt, "createdAt"));
    }

    public String id() {
        return id;
    }

    public String debitAccountId() {
        return debitAccountId;
    }

    public String creditAccountId() {
        return creditAccountId;
    }

    public Money amount() {
        return amount;
    }

    public LedgerRefType refType() {
        return refType;
    }

    public String refId() {
        return refId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }
}
