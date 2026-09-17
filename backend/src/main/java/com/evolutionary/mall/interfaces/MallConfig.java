package com.evolutionary.mall.interfaces;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.application.CampaignRepository;
import com.evolutionary.mall.application.ClaimCouponFromCampaign;
import com.evolutionary.mall.application.CouponTemplateRepository;
import com.evolutionary.mall.application.UserCouponRepository;
import com.evolutionary.mall.domain.Campaign;
import com.evolutionary.mall.domain.CouponKind;
import com.evolutionary.mall.domain.CouponScope;
import com.evolutionary.mall.domain.CouponTemplate;
import com.evolutionary.mall.domain.IssuerType;
import com.evolutionary.mall.infrastructure.InMemoryCampaignRepository;
import com.evolutionary.mall.infrastructure.InMemoryCouponTemplateRepository;
import com.evolutionary.mall.infrastructure.InMemoryUserCouponRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MallConfig {

    private static final Instant VALID_FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant VALID_UNTIL = Instant.parse("2027-01-01T00:00:00Z");

    @Bean
    CampaignRepository campaignRepository() {
        return new InMemoryCampaignRepository();
    }

    @Bean
    CouponTemplateRepository couponTemplateRepository() {
        return new InMemoryCouponTemplateRepository();
    }

    @Bean
    UserCouponRepository userCouponRepository() {
        return new InMemoryUserCouponRepository();
    }

    @Bean
    ClaimCouponFromCampaign claimCouponFromCampaign(
            CampaignRepository campaigns,
            CouponTemplateRepository templates,
            UserCouponRepository userCoupons) {
        return new ClaimCouponFromCampaign(campaigns, templates, userCoupons);
    }

    /**
     * 正式本地种子：CAMP-OK + T-C1（预算 5000¢）；CAMP-EMPTY 供预算耗尽验收。
     */
    @Bean
    ApplicationRunner seedMall(
            CampaignRepository campaigns, CouponTemplateRepository templates) {
        return args -> {
            templates.save(
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

            campaigns.save(
                    Campaign.createActive(
                            "CAMP-OK", "M1", "有预算活动", Money.cny(5_000), List.of("T-C1")));
            campaigns.save(
                    Campaign.createActive(
                            "CAMP-EMPTY", "M1", "空预算活动", Money.cny(0), List.of("T-C1")));
        };
    }
}
