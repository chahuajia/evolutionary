package com.evolutionary.credit.infrastructure;

import com.evolutionary.credit.domain.DebtStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 信用负债持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "credit_ledger_debts")
public class CreditLedgerDebtJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private long amountCents;

    @Column(nullable = false)
    private String amountCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private String billedStatementId;

    @Column
    private Instant paidAt;

    protected CreditLedgerDebtJpaEntity() {}

    public CreditLedgerDebtJpaEntity(
            String id,
            String userId,
            String orderId,
            long amountCents,
            String amountCurrency,
            DebtStatus status,
            Instant createdAt,
            String billedStatementId,
            Instant paidAt) {
        this.id = id;
        this.userId = userId;
        this.orderId = orderId;
        this.amountCents = amountCents;
        this.amountCurrency = amountCurrency;
        this.status = status;
        this.createdAt = createdAt;
        this.billedStatementId = billedStatementId;
        this.paidAt = paidAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public String getAmountCurrency() {
        return amountCurrency;
    }

    public DebtStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getBilledStatementId() {
        return billedStatementId;
    }

    public Instant getPaidAt() {
        return paidAt;
    }
}
