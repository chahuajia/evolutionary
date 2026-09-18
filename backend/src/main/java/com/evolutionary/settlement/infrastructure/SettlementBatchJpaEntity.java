package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.domain.BatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 结算批持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "settlement_batches")
public class SettlementBatchJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private Instant periodStart;

    @Column(nullable = false)
    private Instant periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant closedAt;

    protected SettlementBatchJpaEntity() {}

    public SettlementBatchJpaEntity(
            String id,
            Instant periodStart,
            Instant periodEnd,
            BatchStatus status,
            Instant createdAt,
            Instant closedAt) {
        this.id = id;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = status;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

    public String getId() {
        return id;
    }

    public Instant getPeriodStart() {
        return periodStart;
    }

    public Instant getPeriodEnd() {
        return periodEnd;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }
}
