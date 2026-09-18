package com.evolutionary.credit.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "credit_policies")
public class CreditPolicyJpaEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private int version;

    @Column(nullable = false)
    private long limitACents;

    @Column(nullable = false)
    private long limitBCents;

    @Column(nullable = false)
    private long limitCCents;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private Instant effectiveFrom;

    protected CreditPolicyJpaEntity() {}

    public CreditPolicyJpaEntity(
            String id,
            int version,
            long limitACents,
            long limitBCents,
            long limitCCents,
            String currency,
            Instant effectiveFrom) {
        this.id = id;
        this.version = version;
        this.limitACents = limitACents;
        this.limitBCents = limitBCents;
        this.limitCCents = limitCCents;
        this.currency = currency;
        this.effectiveFrom = effectiveFrom;
    }

    public String getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public long getLimitACents() {
        return limitACents;
    }

    public long getLimitBCents() {
        return limitBCents;
    }

    public long getLimitCCents() {
        return limitCCents;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }
}
