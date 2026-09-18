package com.evolutionary.mall.domain;

import com.evolutionary.commerce.domain.Money;
import java.util.List;
import java.util.Objects;

/** 营销活动（P5-6）；领券扣减 budgetRemaining。 */
public final class Campaign {

    private final String id;
    private final String ownerOrgId;
    private final String name;
    private final Money budgetTotal;
    private final Money budgetRemaining;
    private final CampaignStatus status;
    private final List<String> couponTemplateIds;

    private Campaign(
            String id,
            String ownerOrgId,
            String name,
            Money budgetTotal,
            Money budgetRemaining,
            CampaignStatus status,
            List<String> couponTemplateIds) {
        this.id = id;
        this.ownerOrgId = ownerOrgId;
        this.name = name;
        this.budgetTotal = budgetTotal;
        this.budgetRemaining = budgetRemaining;
        this.status = status;
        this.couponTemplateIds = List.copyOf(couponTemplateIds);
    }

    public static Campaign createActive(
            String id,
            String ownerOrgId,
            String name,
            Money budgetTotal,
            List<String> couponTemplateIds) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("campaign id 不能为空");
        }
        Objects.requireNonNull(budgetTotal, "budgetTotal");
        Objects.requireNonNull(couponTemplateIds, "couponTemplateIds");
        return new Campaign(
                id,
                ownerOrgId,
                name,
                budgetTotal,
                budgetTotal,
                CampaignStatus.ACTIVE,
                couponTemplateIds);
    }

    /** 持久化回放（infrastructure → domain）。 */
    public static Campaign rehydrate(
            String id,
            String ownerOrgId,
            String name,
            Money budgetTotal,
            Money budgetRemaining,
            CampaignStatus status,
            List<String> couponTemplateIds) {
        return new Campaign(
                id,
                ownerOrgId,
                name,
                Objects.requireNonNull(budgetTotal, "budgetTotal"),
                Objects.requireNonNull(budgetRemaining, "budgetRemaining"),
                Objects.requireNonNull(status, "status"),
                Objects.requireNonNull(couponTemplateIds, "couponTemplateIds"));
    }

    public boolean containsTemplate(String templateId) {
        return couponTemplateIds.contains(templateId);
    }

    public boolean isActive() {
        return status == CampaignStatus.ACTIVE;
    }

    /**
     * 领券扣预算；不足则 CAMPAIGN_BUDGET_EXHAUSTED。
     */
    public MallOutcome<Campaign> consumeBudget(Money face) {
        Objects.requireNonNull(face, "face");
        if (!isActive()) {
            return MallOutcome.err(MallErrorCode.CAMPAIGN_NOT_ACTIVE, "活动未激活");
        }
        if (budgetRemaining.cents() < face.cents()) {
            return MallOutcome.err(MallErrorCode.CAMPAIGN_BUDGET_EXHAUSTED, "活动预算已耗尽");
        }
        Money left = new Money(budgetRemaining.cents() - face.cents(), budgetRemaining.currency());
        return MallOutcome.ok(
                new Campaign(
                        id, ownerOrgId, name, budgetTotal, left, status, couponTemplateIds));
    }

    public String id() {
        return id;
    }

    public String ownerOrgId() {
        return ownerOrgId;
    }

    public String name() {
        return name;
    }

    public Money budgetTotal() {
        return budgetTotal;
    }

    public Money budgetRemaining() {
        return budgetRemaining;
    }

    public CampaignStatus status() {
        return status;
    }

    public List<String> couponTemplateIds() {
        return couponTemplateIds;
    }
}
