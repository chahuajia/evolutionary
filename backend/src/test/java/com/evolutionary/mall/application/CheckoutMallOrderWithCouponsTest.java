package com.evolutionary.mall.application;


import com.evolutionary.commerce.domain.Order;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.application.UsageEventRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerInvariant;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.UsageEvent;
import com.evolutionary.mall.domain.CouponKind;
import com.evolutionary.mall.domain.CouponRedemption;
import com.evolutionary.mall.domain.CouponScope;
import com.evolutionary.mall.domain.CouponTemplate;
import com.evolutionary.mall.domain.IssuerType;
import com.evolutionary.mall.domain.MallErrorCode;
import com.evolutionary.mall.domain.MallOrder;
import com.evolutionary.mall.domain.MallOutcome;
import com.evolutionary.mall.domain.MallSku;
import com.evolutionary.mall.domain.UserCoupon;
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

/** AC-42 / 43 / 44 / 47：优惠券核销、互斥、叠加上限与运营商券 scope。 */
class CheckoutMallOrderWithCouponsTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");
    private static final Instant VALID_FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant VALID_UNTIL = Instant.parse("2027-01-01T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);
    /** Fixture：S1 price=50.00 */
    private static final long S1_PRICE_CENTS = 5_000;

    private InMemorySkus skus;
    private InMemoryMallOrders orders;
    private InMemoryUserCoupons userCoupons;
    private InMemoryTemplates templates;
    private InMemoryRedemptions redemptions;
    private InMemoryAccounts accounts;
    private InMemoryLedger ledger;
    private InMemoryEntitlements entitlements;
    private InMemoryUsages usages;
    private CheckoutMallOrderWithCoupons checkout;

    @BeforeEach
    void setUp() {
        skus = new InMemorySkus();
        orders = new InMemoryMallOrders();
        userCoupons = new InMemoryUserCoupons();
        templates = new InMemoryTemplates();
        redemptions = new InMemoryRedemptions();
        accounts = new InMemoryAccounts();
        ledger = new InMemoryLedger();
        entitlements = new InMemoryEntitlements();
        usages = new InMemoryUsages();
        checkout =
                new CheckoutMallOrderWithCoupons(
                        skus,
                        orders,
                        userCoupons,
                        templates,
                        redemptions,
                        accounts,
                        ledger,
                        CLOCK);

        skus.put(MallSku.createOnSale("S1", "M1", "商城配件", Money.cny(S1_PRICE_CENTS), 10));
        accounts.put(
                Account.open(
                        "ACC-U-1",
                        AccountOwnerType.USER,
                        "U1",
                        AccountType.BALANCE,
                        Currency.CNY,
                        20_000));
        accounts.put(
                Account.open(
                        "ACC-M-1",
                        AccountOwnerType.ORG,
                        "M1",
                        AccountType.SETTLEMENT,
                        Currency.CNY,
                        0));

        // C1：满30减5，mutexGroup=MG1，MERCHANT
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
                        "CAMP-1",
                        VALID_FROM,
                        VALID_UNTIL));
        // C2：九折（减 10%），mutexGroup=MG2，MERCHANT
        templates.put(
                CouponTemplate.create(
                        "T-C2",
                        "M1",
                        IssuerType.MERCHANT,
                        CouponKind.PERCENT_OFF,
                        10,
                        null,
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG2",
                        "CAMP-1",
                        VALID_FROM,
                        VALID_UNTIL));
        // C3：同 MG1 的另一张满减，用于互斥
        templates.put(
                CouponTemplate.create(
                        "T-C3",
                        "M1",
                        IssuerType.MERCHANT,
                        CouponKind.FIXED_OFF,
                        200,
                        Money.cny(1_000),
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG1",
                        "CAMP-1",
                        VALID_FROM,
                        VALID_UNTIL));
        // C4：第三张不同互斥组，用于叠加上限
        templates.put(
                CouponTemplate.create(
                        "T-C4",
                        "M1",
                        IssuerType.MERCHANT,
                        CouponKind.FIXED_OFF,
                        100,
                        null,
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG3",
                        "CAMP-1",
                        VALID_FROM,
                        VALID_UNTIL));
        // OP：运营商券，无商城 SKU 列表
        templates.put(
                CouponTemplate.create(
                        "T-OP",
                        "OP1",
                        IssuerType.OPERATOR,
                        CouponKind.FIXED_OFF,
                        500,
                        null,
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG-OP",
                        "CAMP-OP",
                        VALID_FROM,
                        VALID_UNTIL));
    }

    @Test
    @DisplayName("AC-42：满减券核销 — discount=5、paid=45、Redemption 追加、券 used")
    void fixedOffRedemption() {
        userCoupons.put(UserCoupon.issue("UC-C1", "U1", "T-C1"));

        MallOutcome<MallOrder> outcome =
                checkout.execute("U1", "M1", "S1", 1, List.of("UC-C1"));

        assertInstanceOf(MallOutcome.Ok.class, outcome);
        MallOrder order = ((MallOutcome.Ok<MallOrder>) outcome).value();
        assertEquals(MallOrder.Status.PAID, order.status());
        assertEquals(500, order.discountTotal().cents());
        assertEquals(4_500, order.paidAmount().cents());

        assertEquals(UserCoupon.Status.USED, userCoupons.get("UC-C1").status());
        List<CouponRedemption> reds = redemptions.findByOrderId(order.id());
        assertEquals(1, reds.size());
        assertEquals(500, reds.get(0).discountAmount().cents());
        assertEquals("UC-C1", reds.get(0).userCouponId());

        assertEquals(15_500, accounts.get("ACC-U-1").balanceCents());
        assertEquals(4_500, accounts.get("ACC-M-1").balanceCents());
        LedgerInvariant.assertBalanced(ledger.findAll());
        assertTrue(entitlements.isEmpty(), "INV-16：不得创建 Entitlement");
        assertTrue(usages.isEmpty(), "INV-16：不得创建 UsageEvent");
    }

    @Test
    @DisplayName("AC-43：同 mutexGroup 两张券 → COUPON_MUTEX_VIOLATION")
    void mutexViolationRejected() {
        userCoupons.put(UserCoupon.issue("UC-C1", "U1", "T-C1"));
        userCoupons.put(UserCoupon.issue("UC-C3", "U1", "T-C3"));

        MallOutcome<MallOrder> outcome =
                checkout.execute("U1", "M1", "S1", 1, List.of("UC-C1", "UC-C3"));

        assertInstanceOf(MallOutcome.Err.class, outcome);
        assertEquals(
                MallErrorCode.COUPON_MUTEX_VIOLATION,
                ((MallOutcome.Err<MallOrder>) outcome).code());
        assertTrue(orders.all().isEmpty());
        assertEquals(UserCoupon.Status.AVAILABLE, userCoupons.get("UC-C1").status());
        assertTrue(redemptions.findAll().isEmpty());
        assertEquals(10, skus.get("S1").stock());
    }

    @Test
    @DisplayName("AC-44：两张不同互斥组可叠加；第三张 → COUPON_STACK_LIMIT")
    void stackLimitAndTwoCouponsOk() {
        userCoupons.put(UserCoupon.issue("UC-C1", "U1", "T-C1"));
        userCoupons.put(UserCoupon.issue("UC-C2", "U1", "T-C2"));

        MallOutcome<MallOrder> ok =
                checkout.execute("U1", "M1", "S1", 1, List.of("UC-C1", "UC-C2"));
        assertInstanceOf(MallOutcome.Ok.class, ok);
        MallOrder order = ((MallOutcome.Ok<MallOrder>) ok).value();
        // 满减 500 + 九折减 500 = 1000
        assertEquals(1_000, order.discountTotal().cents());
        assertEquals(4_000, order.paidAmount().cents());
        assertEquals(2, redemptions.findByOrderId(order.id()).size());

        // 重置库存与账户后再测第三张
        skus.put(MallSku.createOnSale("S1", "M1", "商城配件", Money.cny(S1_PRICE_CENTS), 10));
        userCoupons.put(UserCoupon.issue("UC-C1b", "U1", "T-C1"));
        userCoupons.put(UserCoupon.issue("UC-C2b", "U1", "T-C2"));
        userCoupons.put(UserCoupon.issue("UC-C4", "U1", "T-C4"));

        MallOutcome<MallOrder> over =
                checkout.execute(
                        "U1", "M1", "S1", 1, List.of("UC-C1b", "UC-C2b", "UC-C4"));
        assertInstanceOf(MallOutcome.Err.class, over);
        assertEquals(
                MallErrorCode.COUPON_STACK_LIMIT,
                ((MallOutcome.Err<MallOrder>) over).code());
    }

    @Test
    @DisplayName("AC-47：运营商券默认作用于商城 → COUPON_SCOPE_MISMATCH")
    void operatorCouponRejectedOnMall() {
        userCoupons.put(UserCoupon.issue("UC-OP", "U1", "T-OP"));

        MallOutcome<MallOrder> outcome =
                checkout.execute("U1", "M1", "S1", 1, List.of("UC-OP"));

        assertInstanceOf(MallOutcome.Err.class, outcome);
        assertEquals(
                MallErrorCode.COUPON_SCOPE_MISMATCH,
                ((MallOutcome.Err<MallOrder>) outcome).code());
        assertTrue(orders.all().isEmpty());
    }

    private static final class InMemorySkus implements MallSkuRepository {
        private final Map<String, MallSku> byId = new HashMap<>();

        void put(MallSku sku) {
            byId.put(sku.id(), sku);
        }

        @Override
        public MallSku get(String skuId) {
            MallSku sku = byId.get(skuId);
            if (sku == null) {
                throw new IllegalArgumentException("unknown sku: " + skuId);
            }
            return sku;
        }

        @Override
        public void save(MallSku sku) {
            byId.put(sku.id(), sku);
        }
    }

    private static final class InMemoryMallOrders implements MallOrderRepository {
        private final Map<String, MallOrder> byId = new HashMap<>();

        @Override
        public void save(MallOrder order) {
            byId.put(order.id(), order);
        }

        @Override
        public Optional<MallOrder> findById(String orderId) {
            return Optional.ofNullable(byId.get(orderId));
        }

        List<MallOrder> all() {
            return List.copyOf(byId.values());
        }
    }

    private static final class InMemoryUserCoupons implements UserCouponRepository {
        private final Map<String, UserCoupon> byId = new HashMap<>();

        void put(UserCoupon c) {
            byId.put(c.id(), c);
        }

        UserCoupon get(String id) {
            return byId.get(id);
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

    private static final class InMemoryRedemptions implements CouponRedemptionRepository {
        private final List<CouponRedemption> all = new ArrayList<>();

        @Override
        public void append(CouponRedemption redemption) {
            all.add(redemption);
        }

        @Override
        public List<CouponRedemption> findByOrderId(String orderId) {
            return all.stream().filter(r -> r.orderId().equals(orderId)).toList();
        }

        @Override
        public List<CouponRedemption> findAll() {
            return List.copyOf(all);
        }
    }

    private static final class InMemoryAccounts implements AccountRepository {
        private final Map<String, Account> byId = new HashMap<>();

        void put(Account account) {
            byId.put(account.id(), account);
        }

        @Override
        public Account get(String accountId) {
            return byId.get(accountId);
        }

        @Override
        public Account findUserBalance(String userId, Currency currency) {
            return byId.values().stream()
                    .filter(
                            a ->
                                    a.ownerType() == AccountOwnerType.USER
                                            && a.ownerId().equals(userId)
                                            && a.type() == AccountType.BALANCE
                                            && a.currency() == currency)
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public Account findUserPoints(String userId, Currency currency) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Account findOrgSettlement(String orgId, Currency currency) {
            return byId.values().stream()
                    .filter(
                            a ->
                                    a.ownerType() == AccountOwnerType.ORG
                                            && a.ownerId().equals(orgId)
                                            && a.type() == AccountType.SETTLEMENT
                                            && a.currency() == currency)
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public void save(Account account) {
            byId.put(account.id(), account);
        }
    }

    private static final class InMemoryLedger implements LedgerRepository {
        private final List<LedgerEntry> entries = new ArrayList<>();

        @Override
        public void append(LedgerEntry entry) {
            entries.add(entry);
        }

        @Override
        public List<LedgerEntry> findAll() {
            return List.copyOf(entries);
        }

        @Override
        public List<LedgerEntry> findByOrderId(String orderId) {
            return entries.stream().filter(e -> e.refId().equals(orderId)).toList();
        }
    }

    private static final class InMemoryEntitlements implements EntitlementRepository {
        private final List<Entitlement> saved = new ArrayList<>();

        boolean isEmpty() {
            return saved.isEmpty();
        }

        @Override
        public void save(Entitlement entitlement) {
            saved.add(entitlement);
        }

        @Override
        public Entitlement get(String entitlementId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Entitlement> findByOrderId(String orderId) {
            return Optional.empty();
        }

        @Override
        public List<Entitlement> findActiveByUser(String userId) {
            return List.of();
        }
    }

    private static final class InMemoryUsages implements UsageEventRepository {
        private final List<UsageEvent> saved = new ArrayList<>();

        boolean isEmpty() {
            return saved.isEmpty();
        }

        @Override
        public void save(UsageEvent event) {
            saved.add(event);
        }

        @Override
        public Optional<UsageEvent> findStartedByBattery(String batteryId) {
            return Optional.empty();
        }

        @Override
        public List<UsageEvent> findStartedByUser(String userId) {
            return List.of();
        }

        @Override
        public Optional<UsageEvent> findStartedByEntitlement(String entitlementId) {
            return Optional.empty();
        }
    }
}
