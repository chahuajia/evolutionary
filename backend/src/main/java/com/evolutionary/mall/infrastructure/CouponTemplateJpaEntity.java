package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.domain.CouponKind;
import com.evolutionary.mall.domain.CouponScope;
import com.evolutionary.mall.domain.IssuerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "coupon_templates")
public class CouponTemplateJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String issuerOrgId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuerType issuerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponKind kind;

    @Column(name = "value_amount", nullable = false)
    private long valueAmount;

    /** null = 无门槛。 */
    private Long minSpendCents;

    private String minSpendCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponScope scope;

    /** 逗号分隔的 scopeId 列表。 */
    @Column(nullable = false)
    private String scopeIdsCsv;

    @Column(nullable = false)
    private String mutexGroup;

    private String campaignId;

    @Column(nullable = false)
    private Instant validFrom;

    @Column(nullable = false)
    private Instant validUntil;

    private Integer perUserLimit;

    protected CouponTemplateJpaEntity() {}

    public CouponTemplateJpaEntity(
            String id,
            String issuerOrgId,
            IssuerType issuerType,
            CouponKind kind,
            long valueAmount,
            Long minSpendCents,
            String minSpendCurrency,
            CouponScope scope,
            String scopeIdsCsv,
            String mutexGroup,
            String campaignId,
            Instant validFrom,
            Instant validUntil,
            Integer perUserLimit) {
        this.id = id;
        this.issuerOrgId = issuerOrgId;
        this.issuerType = issuerType;
        this.kind = kind;
        this.valueAmount = valueAmount;
        this.minSpendCents = minSpendCents;
        this.minSpendCurrency = minSpendCurrency;
        this.scope = scope;
        this.scopeIdsCsv = scopeIdsCsv;
        this.mutexGroup = mutexGroup;
        this.campaignId = campaignId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.perUserLimit = perUserLimit;
    }

    public String getId() {
        return id;
    }

    public String getIssuerOrgId() {
        return issuerOrgId;
    }

    public IssuerType getIssuerType() {
        return issuerType;
    }

    public CouponKind getKind() {
        return kind;
    }

    public long getValueAmount() {
        return valueAmount;
    }

    public Long getMinSpendCents() {
        return minSpendCents;
    }

    public String getMinSpendCurrency() {
        return minSpendCurrency;
    }

    public CouponScope getScope() {
        return scope;
    }

    public String getScopeIdsCsv() {
        return scopeIdsCsv;
    }

    public String getMutexGroup() {
        return mutexGroup;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public Integer getPerUserLimit() {
        return perUserLimit;
    }
}
