package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.LedgerRefType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 分录持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "ledger_entries")
public class LedgerEntryJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String debitAccountId;

    @Column(nullable = false)
    private String creditAccountId;

    @Column(nullable = false)
    private long amountCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency amountCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerRefType refType;

    @Column(nullable = false)
    private String refId;

    @Column(nullable = false)
    private Instant createdAt;

    protected LedgerEntryJpaEntity() {}

    public LedgerEntryJpaEntity(
            String id,
            String debitAccountId,
            String creditAccountId,
            long amountCents,
            Currency amountCurrency,
            LedgerRefType refType,
            String refId,
            Instant createdAt) {
        this.id = id;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.amountCents = amountCents;
        this.amountCurrency = amountCurrency;
        this.refType = refType;
        this.refId = refId;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getDebitAccountId() {
        return debitAccountId;
    }

    public String getCreditAccountId() {
        return creditAccountId;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public Currency getAmountCurrency() {
        return amountCurrency;
    }

    public LedgerRefType getRefType() {
        return refType;
    }

    public String getRefId() {
        return refId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
