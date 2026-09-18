package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.domain.AccrualStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 分润意向持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "profit_share_accruals")
public class ProfitShareAccrualJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String orgId;

    @Column(nullable = false)
    private long amountCents;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private int ruleVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccrualStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant settledAt;

    private String batchId;

    private String reversalOf;

    protected ProfitShareAccrualJpaEntity() {}

    public ProfitShareAccrualJpaEntity(
            String id,
            String orderId,
            String orgId,
            long amountCents,
            String currency,
            int ruleVersion,
            AccrualStatus status,
            Instant createdAt,
            Instant settledAt,
            String batchId,
            String reversalOf) {
        this.id = id;
        this.orderId = orderId;
        this.orgId = orgId;
        this.amountCents = amountCents;
        this.currency = currency;
        this.ruleVersion = ruleVersion;
        this.status = status;
        this.createdAt = createdAt;
        this.settledAt = settledAt;
        this.batchId = batchId;
        this.reversalOf = reversalOf;
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrgId() {
        return orgId;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public String getCurrency() {
        return currency;
    }

    public int getRuleVersion() {
        return ruleVersion;
    }

    public AccrualStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getReversalOf() {
        return reversalOf;
    }
}
