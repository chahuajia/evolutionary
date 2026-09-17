package com.evolutionary.mall.application;

import com.evolutionary.mall.domain.Campaign;
import com.evolutionary.mall.domain.CouponTemplate;
import com.evolutionary.mall.domain.MallErrorCode;
import com.evolutionary.mall.domain.MallOutcome;
import com.evolutionary.mall.domain.UserCoupon;
import java.util.Objects;
import java.util.UUID;

/** 从活动领券（AC-45）；budgetRemaining 不足则拒绝。 */
public final class ClaimCouponFromCampaign {

    private final CampaignRepository campaigns;
    private final CouponTemplateRepository templates;
    private final UserCouponRepository userCoupons;

    public ClaimCouponFromCampaign(
            CampaignRepository campaigns,
            CouponTemplateRepository templates,
            UserCouponRepository userCoupons) {
        this.campaigns = Objects.requireNonNull(campaigns, "campaigns");
        this.templates = Objects.requireNonNull(templates, "templates");
        this.userCoupons = Objects.requireNonNull(userCoupons, "userCoupons");
    }

    public MallOutcome<UserCoupon> execute(String userId, String campaignId, String templateId) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(campaignId, "campaignId");
        Objects.requireNonNull(templateId, "templateId");

        Campaign campaign =
                campaigns
                        .findById(campaignId)
                        .orElse(null);
        if (campaign == null || !campaign.isActive()) {
            return MallOutcome.err(MallErrorCode.CAMPAIGN_NOT_ACTIVE, "活动不存在或未激活");
        }
        if (!campaign.containsTemplate(templateId)) {
            return MallOutcome.err(MallErrorCode.CAMPAIGN_NOT_ACTIVE, "活动不含该券模板");
        }

        CouponTemplate template =
                templates
                        .findById(templateId)
                        .orElse(null);
        if (template == null) {
            return MallOutcome.err(MallErrorCode.COUPON_NOT_AVAILABLE, "券模板不存在");
        }

        MallOutcome<Campaign> consumed = campaign.consumeBudget(template.faceBudget());
        if (consumed instanceof MallOutcome.Err<Campaign> err) {
            return MallOutcome.err(err.code(), err.message());
        }
        Campaign after = ((MallOutcome.Ok<Campaign>) consumed).value();

        UserCoupon issued = UserCoupon.issue(newId("uc"), userId, templateId);
        campaigns.save(after);
        userCoupons.save(issued);
        return MallOutcome.ok(issued);
    }

    private static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
