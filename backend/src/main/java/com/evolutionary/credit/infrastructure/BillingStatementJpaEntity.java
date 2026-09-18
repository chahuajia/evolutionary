package com.evolutionary.credit.infrastructure;

import com.evolutionary.credit.domain.StatementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 周期账单持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "billing_statements")
public class BillingStatementJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Instant periodStart;

    @Column(nullable = false)
    private Instant periodEnd;

    @Column(nullable = false)
    private long totalDueCents;

    @Column(nullable = false)
    private String totalDueCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatementStatus status;

    @Column(nullable = false)
    private Instant dueDate;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant paidAt;

    protected BillingStatementJpaEntity() {}

    public BillingStatementJpaEntity(
            String id,
            String userId,
            Instant periodStart,
            Instant periodEnd,
            long totalDueCents,
            String totalDueCurrency,
            StatementStatus status,
            Instant dueDate,
            Instant createdAt,
            Instant paidAt) {
        this.id = id;
        this.userId = userId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalDueCents = totalDueCents;
        this.totalDueCurrency = totalDueCurrency;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getPeriodStart() {
        return periodStart;
    }

    public Instant getPeriodEnd() {
        return periodEnd;
    }

    public long getTotalDueCents() {
        return totalDueCents;
    }

    public String getTotalDueCurrency() {
        return totalDueCurrency;
    }

    public StatementStatus getStatus() {
        return status;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }
}
