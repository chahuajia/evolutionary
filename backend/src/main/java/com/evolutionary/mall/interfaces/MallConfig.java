package com.evolutionary.mall.interfaces;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.application.CampaignRepository;
import com.evolutionary.mall.application.CheckoutMallOrderWithCoupons;
import com.evolutionary.mall.application.ClaimCouponFromCampaign;
import com.evolutionary.mall.application.CouponRedemptionRepository;
import com.evolutionary.mall.application.CouponTemplateRepository;
import com.evolutionary.mall.application.MallOrderRepository;
import com.evolutionary.mall.application.MallSkuRepository;
import com.evolutionary.mall.application.PurchaseMallOrder;
import com.evolutionary.mall.application.UserCouponRepository;
import com.evolutionary.mall.domain.Campaign;
import com.evolutionary.mall.domain.CouponKind;
import com.evolutionary.mall.domain.CouponScope;
import com.evolutionary.mall.domain.CouponTemplate;
import com.evolutionary.mall.domain.IssuerType;
import com.evolutionary.mall.domain.MallSku;
import com.evolutionary.mall.infrastructure.InMemoryCouponRedemptionRepository;
import com.evolutionary.mall.infrastructure.InMemoryMallOrderRepository;
import com.evolutionary.operator.application.AuditLogRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MallConfig {

    /** MallSkuRepository → {@code JpaMallSkuRepository}（表 mall_skus）。 */

    private static final Instant VALID_FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant VALID_UNTIL = Instant.parse("2027-01-01T00:00:00Z");

    /** CampaignRepository → {@code JpaCampaignRepository}（表 campaigns）。 */

    /** CouponTemplateRepository → {@code JpaCouponTemplateRepository}（表 coupon_templates）。 */

    /** UserCouponRepository → {@code JpaUserCouponRepository}（表 user_coupons）。 */

    @Bean
    MallOrderRepository mallOrderRepository() {
        return new InMemoryMallOrderRepository();
    }

    @Bean
    CouponRedemptionRepository couponRedemptionRepository() {
        return new InMemoryCouponRedemptionRepository();
    }

    @Bean
    ClaimCouponFromCampaign claimCouponFromCampaign(
            CampaignRepository campaigns,
            CouponTemplateRepository templates,
            UserCouponRepository userCoupons,
            AuditLogRepository auditLogs) {
        return new ClaimCouponFromCampaign(
                campaigns, templates, userCoupons, auditLogs, Clock.systemUTC());
    }

    @Bean
    PurchaseMallOrder purchaseMallOrder(
            MallSkuRepository skus,
            MallOrderRepository orders,
            AccountRepository accounts,
            LedgerRepository ledger) {
        return new PurchaseMallOrder(skus, orders, accounts, ledger, Clock.systemUTC());
    }

    @Bean
    CheckoutMallOrderWithCoupons checkoutMallOrderWithCoupons(
            MallSkuRepository skus,
            MallOrderRepository orders,
            UserCouponRepository userCoupons,
            CouponTemplateRepository templates,
            CouponRedemptionRepository redemptions,
            AccountRepository accounts,
            LedgerRepository ledger) {
        return new CheckoutMallOrderWithCoupons(
                skus, orders, userCoupons, templates, redemptions, accounts, ledger, Clock.systemUTC());
    }

    /**
     * 正式本地种子：CAMP-OK + T-C1；SKU S1（M1 · 1000¢）；M1 结算户。
     */
    @Bean
    ApplicationRunner seedMall(
            CampaignRepository campaigns,
            CouponTemplateRepository templates,
            MallSkuRepository skus,
            AccountRepository accounts) {
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

            skus.save(MallSku.createOnSale("S1", "M1", "商城配件", Money.cny(1_000), 20));
            accounts.save(
                    Account.open(
                            "ACC-M1-SETTLE",
                            AccountOwnerType.ORG,
                            "M1",
                            AccountType.SETTLEMENT,
                            Currency.CNY,
                            0));
        };
    }
}
