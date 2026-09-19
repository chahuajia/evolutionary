package com.evolutionary.mall.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.domain.Campaign;
import com.evolutionary.mall.domain.CouponKind;
import com.evolutionary.mall.domain.CouponRedemption;
import com.evolutionary.mall.domain.CouponRules;
import com.evolutionary.mall.domain.CouponScope;
import com.evolutionary.mall.domain.CouponTemplate;
import com.evolutionary.mall.domain.IssuerType;
import com.evolutionary.mall.domain.MallErrorCode;
import com.evolutionary.mall.domain.MallOrder;
import com.evolutionary.mall.domain.MallOutcome;
import com.evolutionary.mall.domain.MallSku;
import com.evolutionary.mall.domain.MerchantProfile;
import com.evolutionary.mall.domain.UserCoupon;
import com.evolutionary.operator.infrastructure.InMemoryAuditLogRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AC-42..47：券核销、互斥、叠加、预算、scope。 */
class CouponCheckoutTest {

    private static final Instant T0 = Instant.parse("2026-09-17T08:00:00Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);

    private InMemorySkus skus;
    private InMemoryMallOrders orders;
    private InMemoryMerchants merchants;
    private InMemoryAccounts accounts;
    private InMemoryLedger ledger;
    private InMemoryTemplates templates;
    private InMemoryUserCoupons userCoupons;
    private InMemoryRedemptions redemptions;
    private InMemoryCampaigns campaigns;
    private CheckoutMallOrderWithCoupons checkout;
    private ClaimCouponFromCampaign claim;

    @BeforeEach
    void setUp() {
        skus = new InMemorySkus();
        orders = new InMemoryMallOrders();
        merchants = new InMemoryMerchants();
        accounts = new InMemoryAccounts();
        ledger = new InMemoryLedger();
        templates = new InMemoryTemplates();
        userCoupons = new InMemoryUserCoupons();
        redemptions = new InMemoryRedemptions();
        campaigns = new InMemoryCampaigns();
        checkout =
                new CheckoutMallOrderWithCoupons(
                        skus,
                        orders,
                        merchants,
                        userCoupons,
                        templates,
                        redemptions,
                        accounts,
                        ledger,
                        CLOCK);
        claim =
                new ClaimCouponFromCampaign(
                        campaigns,
                        templates,
                        userCoupons,
                        new InMemoryAuditLogRepository(),
                        CLOCK);

        skus.put(MallSku.createOnSale("S1", "M1", "配件", Money.cny(5_000), 10));
        merchants.put(MerchantProfile.activate("M1", "演示商家"));
        accounts.put(
                Account.open(
                        "ACC-U",
                        AccountOwnerType.USER,
                        "U1",
                        AccountType.BALANCE,
                        Currency.CNY,
                        20_000));
        accounts.put(
                Account.open(
                        "ACC-M",
                        AccountOwnerType.ORG,
                        "M1",
                        AccountType.SETTLEMENT,
                        Currency.CNY,
                        0));

        Instant from = T0.minusSeconds(3600);
        Instant until = T0.plusSeconds(86400);
        templates.save(
                CouponTemplate.create(
                        "C1",
                        "M1",
                        IssuerType.MERCHANT,
                        CouponKind.FIXED_OFF,
                        500,
                        Money.cny(3_000),
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG1",
                        "CAMP-1",
                        from,
                        until));
        templates.save(
                CouponTemplate.create(
                        "C2",
                        "M1",
                        IssuerType.MERCHANT,
                        CouponKind.PERCENT_OFF,
                        10,
                        null,
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG2",
                        "CAMP-1",
                        from,
                        until));
        templates.save(
                CouponTemplate.create(
                        "C1b",
                        "M1",
                        IssuerType.MERCHANT,
                        CouponKind.FIXED_OFF,
                        100,
                        null,
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG1",
                        "CAMP-1",
                        from,
                        until));
        templates.save(
                CouponTemplate.create(
                        "OP-NO-MALL",
                        "ORG-L1",
                        IssuerType.OPERATOR,
                        CouponKind.FIXED_OFF,
                        200,
                        null,
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG-OP",
                        "CAMP-OP",
                        from,
                        until));
    }

    @Test
    @DisplayName("AC-42：满减券核销 → discount 5.00，paid 45.00，Redemption + used")
    void fixedOffCheckout() {
        userCoupons.save(UserCoupon.issue("UC1", "U1", "C1"));

        MallOutcome<MallOrder> outcome =
                checkout.execute("U1", "M1", "S1", 1, List.of("UC1"));
        assertInstanceOf(MallOutcome.Ok.class, outcome);
        MallOrder order = ((MallOutcome.Ok<MallOrder>) outcome).value();

        assertEquals(500, order.discountTotal().cents());
        assertEquals(4_500, order.paidAmount().cents());
        assertEquals(UserCoupon.Status.USED, userCoupons.findById("UC1").orElseThrow().status());
        List<CouponRedemption> reds = redemptions.findByOrderId(order.id());
        assertEquals(1, reds.size());
        assertEquals(500, reds.get(0).discountAmount().cents());
    }

    @Test
    @DisplayName("AC-43：同 mutexGroup 两张券 → COUPON_MUTEX_VIOLATION")
    void mutexViolation() {
        userCoupons.save(UserCoupon.issue("UC1", "U1", "C1"));
        userCoupons.save(UserCoupon.issue("UC1b", "U1", "C1b"));

        MallOutcome<MallOrder> outcome =
                checkout.execute("U1", "M1", "S1", 1, List.of("UC1", "UC1b"));
        assertInstanceOf(MallOutcome.Err.class, outcome);
        assertEquals(
                MallErrorCode.COUPON_MUTEX_VIOLATION,
                ((MallOutcome.Err<MallOrder>) outcome).code());
    }

    @Test
    @DisplayName("AC-44：不同 mutexGroup 两张可叠加；第三张拒绝 STACK_LIMIT")
    void stackLimit() {
        userCoupons.save(UserCoupon.issue("UC1", "U1", "C1"));
        userCoupons.save(UserCoupon.issue("UC2", "U1", "C2"));

        MallOutcome<MallOrder> ok =
                checkout.execute("U1", "M1", "S1", 1, List.of("UC1", "UC2"));
        assertInstanceOf(MallOutcome.Ok.class, ok);
        MallOrder order = ((MallOutcome.Ok<MallOrder>) ok).value();
        // 满减 500 + 10% 折扣 500 = 1000；paid 4000
        assertEquals(1_000, order.discountTotal().cents());
        assertEquals(4_000, order.paidAmount().cents());

        skus.put(MallSku.createOnSale("S1", "M1", "配件", Money.cny(5_000), 10));
        accounts.put(
                Account.open(
                        "ACC-U",
                        AccountOwnerType.USER,
                        "U1",
                        AccountType.BALANCE,
                        Currency.CNY,
                        20_000));
        userCoupons.save(UserCoupon.issue("UC1x", "U1", "C1"));
        userCoupons.save(UserCoupon.issue("UC2x", "U1", "C2"));
        userCoupons.save(UserCoupon.issue("UC3x", "U1", "C1b"));

        MallOutcome<MallOrder> rejected =
                checkout.execute("U1", "M1", "S1", 1, List.of("UC1x", "UC2x", "UC3x"));
        assertInstanceOf(MallOutcome.Err.class, rejected);
        assertEquals(
                MallErrorCode.COUPON_STACK_LIMIT,
                ((MallOutcome.Err<MallOrder>) rejected).code());
    }

    @Test
    @DisplayName("AC-45：活动预算 0 → CAMPAIGN_BUDGET_EXHAUSTED")
    void campaignBudgetExhausted() {
        campaigns.save(
                Campaign.createActive("CAMP-0", "M1", "空预算", Money.cny(0), List.of("C1")));

        MallOutcome<UserCoupon> outcome = claim.execute("U1", "CAMP-0", "C1");
        assertInstanceOf(MallOutcome.Err.class, outcome);
        assertEquals(
                MallErrorCode.CAMPAIGN_BUDGET_EXHAUSTED,
                ((MallOutcome.Err<UserCoupon>) outcome).code());
    }

    @Test
    @DisplayName("AC-46：MERCHANT 券用于换电套餐 → COUPON_SCOPE_MISMATCH")
    void merchantCouponNotForBattery() {
        CouponTemplate c1 = templates.findById("C1").orElseThrow();
        MallOutcome<Void> outcome = CouponRules.assertApplicableToPackageTemplate(c1);
        assertInstanceOf(MallOutcome.Err.class, outcome);
        assertEquals(
                MallErrorCode.COUPON_SCOPE_MISMATCH,
                ((MallOutcome.Err<Void>) outcome).code());
    }

    @Test
    @DisplayName("AC-47：OPERATOR 券无商城显式 SKU_LIST → COUPON_SCOPE_MISMATCH")
    void operatorCouponDefaultNotForMall() {
        userCoupons.save(UserCoupon.issue("UCOP", "U1", "OP-NO-MALL"));
        MallOutcome<MallOrder> outcome =
                checkout.execute("U1", "M1", "S1", 1, List.of("UCOP"));
        assertInstanceOf(MallOutcome.Err.class, outcome);
        assertEquals(
                MallErrorCode.COUPON_SCOPE_MISMATCH,
                ((MallOutcome.Err<MallOrder>) outcome).code());
    }

    @Test
    @DisplayName("领券成功扣减预算")
    void claimConsumesBudget() {
        campaigns.save(
                Campaign.createActive(
                        "CAMP-1", "M1", "满减活动", Money.cny(5_000), List.of("C1")));
        MallOutcome<UserCoupon> outcome = claim.execute("U1", "CAMP-1", "C1");
        assertInstanceOf(MallOutcome.Ok.class, outcome);
        assertTrue(((MallOutcome.Ok<UserCoupon>) outcome).value().isAvailable());
        assertEquals(4_500, campaigns.findById("CAMP-1").orElseThrow().budgetRemaining().cents());
    }

    private static final class InMemorySkus implements MallSkuRepository {
        private final Map<String, MallSku> store = new HashMap<>();

        void put(MallSku sku) {
            store.put(sku.id(), sku);
        }

        @Override
        public void save(MallSku sku) {
            store.put(sku.id(), sku);
        }

        @Override
        public MallSku get(String id) {
            MallSku sku = store.get(id);
            if (sku == null) {
                throw new IllegalArgumentException("unknown sku");
            }
            return sku;
        }
    }

    private static final class InMemoryMallOrders implements MallOrderRepository {
        private final Map<String, MallOrder> store = new HashMap<>();

        @Override
        public void save(MallOrder order) {
            store.put(order.id(), order);
        }

        @Override
        public Optional<MallOrder> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }
    }

    private static final class InMemoryMerchants implements MerchantProfileRepository {
        private final Map<String, MerchantProfile> byOrgId = new HashMap<>();

        void put(MerchantProfile profile) {
            byOrgId.put(profile.orgId(), profile);
        }

        @Override
        public void save(MerchantProfile profile) {
            byOrgId.put(profile.orgId(), profile);
        }

        @Override
        public Optional<MerchantProfile> findByOrgId(String orgId) {
            return Optional.ofNullable(byOrgId.get(orgId));
        }
    }

    private static final class InMemoryAccounts implements AccountRepository {
        private final Map<String, Account> store = new HashMap<>();

        void put(Account a) {
            store.put(a.id(), a);
        }

        @Override
        public Account get(String accountId) {
            return store.get(accountId);
        }

        @Override
        public void save(Account account) {
            store.put(account.id(), account);
        }

        @Override
        public Account findUserBalance(String userId, Currency currency) {
            return store.values().stream()
                    .filter(a -> a.ownerType() == AccountOwnerType.USER)
                    .filter(a -> a.ownerId().equals(userId))
                    .filter(a -> a.type() == AccountType.BALANCE)
                    .filter(a -> a.currency() == currency)
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public Account findUserPoints(String userId, Currency currency) {
            return store.values().stream()
                    .filter(a -> a.ownerType() == AccountOwnerType.USER)
                    .filter(a -> a.ownerId().equals(userId))
                    .filter(a -> a.type() == AccountType.POINTS)
                    .filter(a -> a.currency() == currency)
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public Account findOrgSettlement(String orgId, Currency currency) {
            return store.values().stream()
                    .filter(a -> a.ownerType() == AccountOwnerType.ORG)
                    .filter(a -> a.ownerId().equals(orgId))
                    .filter(a -> a.type() == AccountType.SETTLEMENT)
                    .filter(a -> a.currency() == currency)
                    .findFirst()
                    .orElseThrow();
        }
    }

    private static final class InMemoryLedger implements LedgerRepository {
        private final List<LedgerEntry> store = new ArrayList<>();

        @Override
        public void append(LedgerEntry entry) {
            store.add(entry);
        }

        @Override
        public List<LedgerEntry> findAll() {
            return List.copyOf(store);
        }

        @Override
        public List<LedgerEntry> findByOrderId(String orderId) {
            return store.stream().filter(e -> orderId.equals(e.refId())).toList();
        }
    }

    private static final class InMemoryTemplates implements CouponTemplateRepository {
        private final Map<String, CouponTemplate> store = new HashMap<>();

        @Override
        public void save(CouponTemplate template) {
            store.put(template.id(), template);
        }

        @Override
        public Optional<CouponTemplate> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }
    }

    private static final class InMemoryUserCoupons implements UserCouponRepository {
        private final Map<String, UserCoupon> store = new HashMap<>();

        @Override
        public void save(UserCoupon coupon) {
            store.put(coupon.id(), coupon);
        }

        @Override
        public Optional<UserCoupon> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }
    }

    private static final class InMemoryRedemptions implements CouponRedemptionRepository {
        private final List<CouponRedemption> store = new ArrayList<>();

        @Override
        public void append(CouponRedemption redemption) {
            store.add(redemption);
        }

        @Override
        public List<CouponRedemption> findByOrderId(String orderId) {
            return store.stream().filter(r -> r.orderId().equals(orderId)).toList();
        }

        @Override
        public List<CouponRedemption> findAll() {
            return List.copyOf(store);
        }
    }

    private static final class InMemoryCampaigns implements CampaignRepository {
        private final Map<String, Campaign> store = new HashMap<>();

        @Override
        public void save(Campaign campaign) {
            store.put(campaign.id(), campaign);
        }

        @Override
        public Optional<Campaign> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }
    }
}
