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

    public static LedgerEntry orderPayment(
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
                LedgerRefType.ORDER_PAYMENT,
                requireId(orderId),
                Objects.requireNonNull(createdAt, "createdAt"));
    }

    /** 退款反向分录：debit Org.SETTLEMENT，credit User.BALANCE；refId 同 orderId。 */
    public static LedgerEntry orderRefund(
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
                LedgerRefType.ORDER_REFUND,
                requireId(orderId),
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
