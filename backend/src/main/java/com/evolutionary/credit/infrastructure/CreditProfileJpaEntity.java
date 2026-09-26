package com.evolutionary.credit.infrastructure;


import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.credit.domain.ScoreTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 信用档案持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "credit_profiles")
public class CreditProfileJpaEntity {

    @Id
    private String userId;

    @Column(nullable = false)
    private long creditLimitCents;

    @Column(nullable = false)
    private String creditLimitCurrency;

    @Column(nullable = false)
    private long usedCreditCents;

    @Column(nullable = false)
    private String usedCreditCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditProfile.Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScoreTier scoreTier;

    @Column(nullable = false)
    private int policyVersion;

    protected CreditProfileJpaEntity() {}

    public CreditProfileJpaEntity(
            String userId,
            long creditLimitCents,
            String creditLimitCurrency,
            long usedCreditCents,
            String usedCreditCurrency,
            CreditProfile.Status status,
            ScoreTier scoreTier,
            int policyVersion) {
        this.userId = userId;
        this.creditLimitCents = creditLimitCents;
        this.creditLimitCurrency = creditLimitCurrency;
        this.usedCreditCents = usedCreditCents;
        this.usedCreditCurrency = usedCreditCurrency;
        this.status = status;
        this.scoreTier = scoreTier;
        this.policyVersion = policyVersion;
    }

    public String getUserId() {
        return userId;
    }

    public long getCreditLimitCents() {
        return creditLimitCents;
    }

    public String getCreditLimitCurrency() {
        return creditLimitCurrency;
    }

    public long getUsedCreditCents() {
        return usedCreditCents;
    }

    public String getUsedCreditCurrency() {
        return usedCreditCurrency;
    }

    public CreditProfile.Status getStatus() {
        return status;
    }

    public ScoreTier getScoreTier() {
        return scoreTier;
    }

    public int getPolicyVersion() {
        return policyVersion;
    }
}
