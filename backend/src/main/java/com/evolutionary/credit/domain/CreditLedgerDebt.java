package com.evolutionary.credit.domain;

import com.evolutionary.commerce.domain.Money;
import java.time.Instant;
import java.util.Objects;

/** 信用负债分录（append-only 状态迁移，INV-17）。 */
public final class CreditLedgerDebt {

    /**
     * 信用负债分录状态。
     */
    public enum Status {
        OPEN,
        BILLED,
        PAID,
        WRITTEN_OFF
    }


    private final String id;
    private final String userId;
    private final String orderId;
    private final Money amount;
    private final CreditLedgerDebt.Status status;
    private final Instant createdAt;
    private final String billedStatementId;
    private final Instant paidAt;

    private CreditLedgerDebt(
            String id,
            String userId,
            String orderId,
            Money amount,
            CreditLedgerDebt.Status status,
            Instant createdAt,
            String billedStatementId,
            Instant paidAt) {
        this.id = id;
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.billedStatementId = billedStatementId;
        this.paidAt = paidAt;
    }

    public static CreditLedgerDebt open(
            String id, String userId, String orderId, Money amount, Instant createdAt) {
        return new CreditLedgerDebt(
                Objects.requireNonNull(id, "id"),
                Objects.requireNonNull(userId, "userId"),
                Objects.requireNonNull(orderId, "orderId"),
                Objects.requireNonNull(amount, "amount"),
                CreditLedgerDebt.Status.OPEN,
                Objects.requireNonNull(createdAt, "createdAt"),
                null,
                null);
    }

    /** 持久层全字段回放（仅 infrastructure 使用）。 */
    public static CreditLedgerDebt rehydrate(
            String id,
            String userId,
            String orderId,
            Money amount,
            CreditLedgerDebt.Status status,
            Instant createdAt,
            String billedStatementId,
            Instant paidAt) {
        return new CreditLedgerDebt(
                Objects.requireNonNull(id, "id"),
                Objects.requireNonNull(userId, "userId"),
                Objects.requireNonNull(orderId, "orderId"),
                Objects.requireNonNull(amount, "amount"),
                Objects.requireNonNull(status, "status"),
                Objects.requireNonNull(createdAt, "createdAt"),
                billedStatementId,
                paidAt);
    }

    public CreditLedgerDebt markBilled(String statementId) {
        if (status != CreditLedgerDebt.Status.OPEN) {
            throw new IllegalStateException("仅 OPEN 可出账");
        }
        return new CreditLedgerDebt(
                id,
                userId,
                orderId,
                amount,
                CreditLedgerDebt.Status.BILLED,
                createdAt,
                Objects.requireNonNull(statementId, "statementId"),
                null);
    }

    public CreditLedgerDebt markPaid(Instant at) {
        if (status != CreditLedgerDebt.Status.BILLED && status != CreditLedgerDebt.Status.OPEN) {
            throw new IllegalStateException("仅 BILLED/OPEN 可清账");
        }
        return new CreditLedgerDebt(
                id,
                userId,
                orderId,
                amount,
                CreditLedgerDebt.Status.PAID,
                createdAt,
                billedStatementId,
                Objects.requireNonNull(at, "paidAt"));
    }

    /** 订单退款核销未出账负债（OPEN → WRITTEN_OFF）。 */
    public CreditLedgerDebt writeOff() {
        if (status != CreditLedgerDebt.Status.OPEN) {
            throw new IllegalStateException("仅 OPEN 可核销");
        }
        return new CreditLedgerDebt(
                id, userId, orderId, amount, CreditLedgerDebt.Status.WRITTEN_OFF, createdAt, null, null);
    }

    public String id() {
        return id;
    }

    public String userId() {
        return userId;
    }

    public String orderId() {
        return orderId;
    }

    public Money amount() {
        return amount;
    }

    public CreditLedgerDebt.Status status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String billedStatementId() {
        return billedStatementId;
    }

    public Instant paidAt() {
        return paidAt;
    }
}
