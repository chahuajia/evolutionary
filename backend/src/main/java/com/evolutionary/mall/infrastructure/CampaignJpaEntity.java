package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.domain.CampaignStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "campaigns")
public class CampaignJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String ownerOrgId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long budgetTotalCents;

    @Column(nullable = false)
    private String budgetTotalCurrency;

    @Column(nullable = false)
    private long budgetRemainingCents;

    @Column(nullable = false)
    private String budgetRemainingCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status;

    /** 逗号分隔的 couponTemplateId 列表。 */
    @Column(nullable = false)
    private String couponTemplateIdsCsv;

    protected CampaignJpaEntity() {}

    public CampaignJpaEntity(
            String id,
            String ownerOrgId,
            String name,
            long budgetTotalCents,
            String budgetTotalCurrency,
            long budgetRemainingCents,
            String budgetRemainingCurrency,
            CampaignStatus status,
            String couponTemplateIdsCsv) {
        this.id = id;
        this.ownerOrgId = ownerOrgId;
        this.name = name;
        this.budgetTotalCents = budgetTotalCents;
        this.budgetTotalCurrency = budgetTotalCurrency;
        this.budgetRemainingCents = budgetRemainingCents;
        this.budgetRemainingCurrency = budgetRemainingCurrency;
        this.status = status;
        this.couponTemplateIdsCsv = couponTemplateIdsCsv;
    }

    public String getId() {
        return id;
    }

    public String getOwnerOrgId() {
        return ownerOrgId;
    }

    public String getName() {
        return name;
    }

    public long getBudgetTotalCents() {
        return budgetTotalCents;
    }

    public String getBudgetTotalCurrency() {
        return budgetTotalCurrency;
    }

    public long getBudgetRemainingCents() {
        return budgetRemainingCents;
    }

    public String getBudgetRemainingCurrency() {
        return budgetRemainingCurrency;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public String getCouponTemplateIdsCsv() {
        return couponTemplateIdsCsv;
    }
}
