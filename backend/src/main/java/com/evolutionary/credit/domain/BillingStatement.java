package com.evolutionary.credit.domain;

import com.evolutionary.commerce.domain.Money;
import java.time.Instant;
import java.util.Objects;

/** 周期账单（P6-3）。 */
public final class BillingStatement {

    /**
     * 账单状态。
     */
    public enum Status {
        OPEN,
        DUE,
        PAID,
        OVERDUE
    }


    private final String id;
    private final String userId;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final Money totalDue;
    private final BillingStatement.Status status;
    private final Instant dueDate;
    private final Instant createdAt;
    private final Instant paidAt;

    private BillingStatement(
            String id,
            String userId,
            Instant periodStart,
            Instant periodEnd,
            Money totalDue,
            BillingStatement.Status status,
            Instant dueDate,
            Instant createdAt,
            Instant paidAt) {
        this.id = id;
        this.userId = userId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalDue = totalDue;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    /** 出账：宽限期 7 天。 */
    public static BillingStatement issueDue(
            String id,
            String userId,
            Instant periodStart,
            Instant periodEnd,
            Money totalDue,
            Instant createdAt) {
        Instant due = Objects.requireNonNull(periodEnd, "periodEnd").plusSeconds(7L * 86_400);
        return new BillingStatement(
                Objects.requireNonNull(id, "id"),
                Objects.requireNonNull(userId, "userId"),
                Objects.requireNonNull(periodStart, "periodStart"),
                periodEnd,
                Objects.requireNonNull(totalDue, "totalDue"),
                BillingStatement.Status.DUE,
                due,
                Objects.requireNonNull(createdAt, "createdAt"),
                null);
    }

    /** 持久层全字段回放（仅 infrastructure 使用）。 */
    public static BillingStatement rehydrate(
            String id,
            String userId,
            Instant periodStart,
            Instant periodEnd,
            Money totalDue,
            BillingStatement.Status status,
            Instant dueDate,
            Instant createdAt,
            Instant paidAt) {
        return new BillingStatement(
                Objects.requireNonNull(id, "id"),
                Objects.requireNonNull(userId, "userId"),
                Objects.requireNonNull(periodStart, "periodStart"),
                Objects.requireNonNull(periodEnd, "periodEnd"),
                Objects.requireNonNull(totalDue, "totalDue"),
                Objects.requireNonNull(status, "status"),
                Objects.requireNonNull(dueDate, "dueDate"),
                Objects.requireNonNull(createdAt, "createdAt"),
                paidAt);
    }

    public BillingStatement markPaid(Instant at) {
        if (status != BillingStatement.Status.DUE && status != BillingStatement.Status.OVERDUE) {
            throw new IllegalStateException("仅 DUE/OVERDUE 可还款");
        }
        return new BillingStatement(
                id,
                userId,
                periodStart,
                periodEnd,
                totalDue,
                BillingStatement.Status.PAID,
                dueDate,
                createdAt,
                Objects.requireNonNull(at, "paidAt"));
    }

    public BillingStatement markOverdue() {
        if (status != BillingStatement.Status.DUE) {
            throw new IllegalStateException("仅 DUE 可逾期");
        }
        return new BillingStatement(
                id,
                userId,
                periodStart,
                periodEnd,
                totalDue,
                BillingStatement.Status.OVERDUE,
                dueDate,
                createdAt,
                null);
    }

    public boolean isPastDue(Instant at) {
        return (status == BillingStatement.Status.DUE || status == BillingStatement.Status.OVERDUE)
                && at.isAfter(dueDate);
    }

    public String id() {
        return id;
    }

    public String userId() {
        return userId;
    }

    public Instant periodStart() {
        return periodStart;
    }

    public Instant periodEnd() {
        return periodEnd;
    }

    public Money totalDue() {
        return totalDue;
    }

    public BillingStatement.Status status() {
        return status;
    }

    public Instant dueDate() {
        return dueDate;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant paidAt() {
        return paidAt;
    }
}
