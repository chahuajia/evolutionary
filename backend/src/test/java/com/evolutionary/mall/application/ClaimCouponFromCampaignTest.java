package com.evolutionary.mall.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.domain.Campaign;
import com.evolutionary.mall.domain.CouponKind;
import com.evolutionary.mall.domain.CouponScope;
import com.evolutionary.mall.domain.CouponTemplate;
import com.evolutionary.mall.domain.IssuerType;
import com.evolutionary.mall.domain.MallErrorCode;
import com.evolutionary.mall.domain.MallOutcome;
import com.evolutionary.mall.domain.UserCoupon;
import com.evolutionary.operator.infrastructure.InMemoryAuditLogRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AC-45：Campaign 预算耗尽拒绝领券。 */
class ClaimCouponFromCampaignTest {

    private static final Instant VALID_FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant VALID_UNTIL = Instant.parse("2027-01-01T00:00:00Z");

    private InMemoryCampaigns campaigns;
    private InMemoryTemplates templates;
    private InMemoryUserCoupons userCoupons;
    private ClaimCouponFromCampaign claim;

    @BeforeEach
    void setUp() {
        campaigns = new InMemoryCampaigns();
        templates = new InMemoryTemplates();
        userCoupons = new InMemoryUserCoupons();
        claim =
                new ClaimCouponFromCampaign(
                        campaigns,
                        templates,
                        userCoupons,
                        new InMemoryAuditLogRepository(),
                        Clock.fixed(VALID_FROM, ZoneOffset.UTC));

        templates.put(
                CouponTemplate.create(
                        "T-C1",
                        "M1",
                        IssuerType.MERCHANT,
                        CouponKind.FIXED_OFF,
                        500,
                        Money.cny(3_000),
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG1",
                        "CAMP-EMPTY",
                        VALID_FROM,
                        VALID_UNTIL));
        campaigns.put(
                Campaign.createActive(
                        "CAMP-EMPTY", "M1", "空预算活动", Money.cny(0), List.of("T-C1")));
        campaigns.put(
                Campaign.createActive(
                        "CAMP-OK", "M1", "有预算活动", Money.cny(5_000), List.of("T-C1")));
    }

    @Test
    @DisplayName("AC-45：budgetRemaining=0 领券 → CAMPAIGN_BUDGET_EXHAUSTED")
    void budgetExhausted() {
        MallOutcome<UserCoupon> outcome = claim.execute("U1", "CAMP-EMPTY", "T-C1");

        assertInstanceOf(MallOutcome.Err.class, outcome);
        assertEquals(
                MallErrorCode.CAMPAIGN_BUDGET_EXHAUSTED,
                ((MallOutcome.Err<UserCoupon>) outcome).code());
        assertEquals(0, campaigns.get("CAMP-EMPTY").budgetRemaining().cents());
        assertEquals(0, userCoupons.size());
    }

    @Test
    @DisplayName("有预算时可领券并扣减 budgetRemaining")
    void claimSucceedsAndConsumesBudget() {
        // 同一模板挂到有预算活动（测试内覆盖 campaignId 关联仅靠活动列表）
        templates.put(
                CouponTemplate.create(
                        "T-C1",
                        "M1",
                        IssuerType.MERCHANT,
                        CouponKind.FIXED_OFF,
                        500,
                        Money.cny(3_000),
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG1",
                        "CAMP-OK",
                        VALID_FROM,
                        VALID_UNTIL));

        MallOutcome<UserCoupon> outcome = claim.execute("U1", "CAMP-OK", "T-C1");

        assertInstanceOf(MallOutcome.Ok.class, outcome);
        assertEquals(4_500, campaigns.get("CAMP-OK").budgetRemaining().cents());
        assertEquals(1, userCoupons.size());
    }

    private static final class InMemoryCampaigns implements CampaignRepository {
        private final Map<String, Campaign> byId = new HashMap<>();

        void put(Campaign c) {
            byId.put(c.id(), c);
        }

        Campaign get(String id) {
            return byId.get(id);
        }

        @Override
        public Optional<Campaign> findById(String campaignId) {
            return Optional.ofNullable(byId.get(campaignId));
        }

        @Override
        public void save(Campaign campaign) {
            byId.put(campaign.id(), campaign);
        }
    }

    private static final class InMemoryTemplates implements CouponTemplateRepository {
        private final Map<String, CouponTemplate> byId = new HashMap<>();

        void put(CouponTemplate t) {
            byId.put(t.id(), t);
        }

        @Override
        public Optional<CouponTemplate> findById(String templateId) {
            return Optional.ofNullable(byId.get(templateId));
        }

        @Override
        public void save(CouponTemplate template) {
            byId.put(template.id(), template);
        }
    }

    private static final class InMemoryUserCoupons implements UserCouponRepository {
        private final Map<String, UserCoupon> byId = new HashMap<>();

        int size() {
            return byId.size();
        }

        @Override
        public Optional<UserCoupon> findById(String userCouponId) {
            return Optional.ofNullable(byId.get(userCouponId));
        }

        @Override
        public void save(UserCoupon userCoupon) {
            byId.put(userCoupon.id(), userCoupon);
        }
    }
}
