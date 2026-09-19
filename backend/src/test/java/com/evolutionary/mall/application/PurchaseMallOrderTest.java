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
import com.evolutionary.mall.domain.MallErrorCode;
import com.evolutionary.mall.domain.MallOrder;
import com.evolutionary.mall.domain.MallOutcome;
import com.evolutionary.mall.domain.MallSku;
import com.evolutionary.mall.domain.MerchantProfile;
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

/** AC-41 / INV-16：商城下单 PAID 后无 Entitlement / UsageEvent。 */
class PurchaseMallOrderTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);
    /** Fixture：S1 price=50.00 */
    private static final long S1_PRICE_CENTS = 5_000;

    private InMemorySkus skus;
    private InMemoryMallOrders orders;
    private InMemoryMerchants merchants;
    private InMemoryAccounts accounts;
    private InMemoryLedger ledger;
    private InMemoryEntitlements entitlements;
    private InMemoryUsages usages;
    private PurchaseMallOrder purchase;

    @BeforeEach
    void setUp() {
        skus = new InMemorySkus();
        orders = new InMemoryMallOrders();
        merchants = new InMemoryMerchants();
        accounts = new InMemoryAccounts();
        ledger = new InMemoryLedger();
        entitlements = new InMemoryEntitlements();
        usages = new InMemoryUsages();
        // INV-16：用例构造不传入 entitlements / usages
        purchase = new PurchaseMallOrder(skus, orders, merchants, accounts, ledger, CLOCK);

        skus.put(
                MallSku.createOnSale(
                        "S1", "M1", "商城配件", Money.cny(S1_PRICE_CENTS), 10));
        merchants.put(MerchantProfile.activate("M1", "演示商家"));
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
    }

    @Test
    @DisplayName("AC-41：PAID 后 paidAmount 正确，且不产生 Entitlement / UsageEvent")
    void paidOrderCreatesNoEntitlementOrUsage() {
        MallOutcome<MallOrder> outcome = purchase.execute("U1", "M1", "S1", 1);

        assertInstanceOf(MallOutcome.Ok.class, outcome);
        MallOrder order = ((MallOutcome.Ok<MallOrder>) outcome).value();

        assertEquals(MallOrder.Status.PAID, order.status());
        assertEquals(S1_PRICE_CENTS, order.paidAmount().cents());
        assertEquals(Currency.CNY, order.paidAmount().currency());
        assertEquals(1, order.lines().size());
        assertEquals("S1", order.lines().get(0).skuId());

        assertEquals(15_000, accounts.get("ACC-U-1").balanceCents());
        assertEquals(S1_PRICE_CENTS, accounts.get("ACC-M-1").balanceCents());
        assertEquals(9, skus.get("S1").stock());
        LedgerInvariant.assertBalanced(ledger.findAll());

        // INV-16：旁路探测仓始终为空（用例未注入、亦未写入）
        assertTrue(entitlements.isEmpty(), "不得创建 Entitlement");
        assertTrue(usages.isEmpty(), "不得创建 UsageEvent");
        assertTrue(entitlements.findByOrderId(order.id()).isEmpty());
    }

    @Test
    @DisplayName("库存不足时拒绝下单")
    void insufficientStockRejected() {
        MallOutcome<MallOrder> outcome = purchase.execute("U1", "M1", "S1", 100);

        assertInstanceOf(MallOutcome.Err.class, outcome);
        assertTrue(orders.all().isEmpty());
        assertEquals(10, skus.get("S1").stock());
        assertTrue(entitlements.isEmpty());
        assertTrue(usages.isEmpty());
    }

    @Test
    @DisplayName("商家停用时拒绝下单")
    void suspendedMerchantRejected() {
        merchants.put(
                MerchantProfile.rehydrate(
                        "M1", "演示商家", MerchantProfile.Status.SUSPENDED));

        MallOutcome<MallOrder> outcome = purchase.execute("U1", "M1", "S1", 1);

        assertInstanceOf(MallOutcome.Err.class, outcome);
        MallOutcome.Err<MallOrder> err = (MallOutcome.Err<MallOrder>) outcome;
        assertEquals(MallErrorCode.MERCHANT_NOT_ACTIVE, err.code());
        assertTrue(orders.all().isEmpty());
        assertEquals(10, skus.get("S1").stock());
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
            return byId.values().stream()
                    .filter(
                            a ->
                                    a.ownerType() == AccountOwnerType.USER
                                            && a.ownerId().equals(userId)
                                            && a.type() == AccountType.POINTS
                                            && a.currency() == currency)
                    .findFirst()
                    .orElseThrow();
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

    /** 旁路探测：证明 PurchaseMallOrder 未写入权益。 */
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
            return saved.stream().filter(e -> e.orderId().equals(orderId)).findFirst();
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
