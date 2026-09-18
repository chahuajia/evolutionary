package com.evolutionary.commerce.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.BatteryAsset;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.EntitlementStatus;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.MeteringMode;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.commerce.domain.ProductStatus;
import com.evolutionary.commerce.domain.UsageEvent;
import com.evolutionary.iot.application.AssertShadowFreshForMetered;
import com.evolutionary.iot.application.DeviceShadowRepository;
import com.evolutionary.iot.application.ShadowTelemetryFreshnessAdapter;
import com.evolutionary.iot.domain.DeviceShadow;
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

/** AC-58：stale 影子禁止 P3 计量换电。 */
class StaleTelemetryBlocksMeteredSwapTest {

    private static final Instant T0 = Instant.parse("2026-09-17T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);

    private InMemoryEntitlements entitlements;
    private InMemoryBatteries batteries;
    private InMemoryUsages usages;
    private InMemoryProducts products;
    private InMemoryAccounts accounts;
    private InMemoryLedger ledger;
    private InMemoryShadows shadows;

    @BeforeEach
    void setUp() {
        entitlements = new InMemoryEntitlements();
        batteries = new InMemoryBatteries();
        usages = new InMemoryUsages();
        products = new InMemoryProducts();
        accounts = new InMemoryAccounts();
        ledger = new InMemoryLedger();
        shadows = new InMemoryShadows();

        products.put(
                Product.createMetered(
                        "P-3", "ORG-1", "按电量计费", Money.cny(50), ProductStatus.PUBLISHED));
        batteries.put(BatteryAsset.createIdle("B1", "ORG-1", "v", "m"));
        accounts.put(
                Account.open(
                        "ACC-U-1",
                        AccountOwnerType.USER,
                        "U-1",
                        AccountType.BALANCE,
                        Currency.CNY,
                        20_000));
        accounts.put(
                Account.open(
                        "ACC-O-1",
                        AccountOwnerType.ORG,
                        "ORG-1",
                        AccountType.SETTLEMENT,
                        Currency.CNY,
                        0));
        entitlements.put(
                Entitlement.rehydrate(
                        "E-P3",
                        "O-P3",
                        "U-1",
                        "P-3",
                        T0.minusSeconds(60),
                        null,
                        EntitlementStatus.ACTIVE,
                        null,
                        MeteringMode.PAY_AS_YOU_GO));
    }

    @Test
    @DisplayName("AC-58：shadow stale → TELEMETRY_STALE，无分录无 COMPLETED")
    void staleRejectsMeteredSwap() {
        Instant old = T0.minus(DeviceShadow.STALE_AFTER).minusSeconds(1);
        shadows.save(DeviceShadow.seed("B1", "vendorA", "ext-1", 80, 4200, old));

        PerformEntitledSwap swap = meteredSwap();
        DomainOutcome<UsageEvent> outcome = swap.execute("U-1", "E-P3", "CAB-1", 80, 60);

        assertInstanceOf(DomainOutcome.Err.class, outcome);
        assertEquals(
                DomainErrorCode.TELEMETRY_STALE,
                ((DomainOutcome.Err<UsageEvent>) outcome).code());
        assertTrue(ledger.findAll().isEmpty());
        assertTrue(usages.all().isEmpty());
        assertEquals(20_000, accounts.get("ACC-U-1").balanceCents());
    }

    @Test
    @DisplayName("新鲜影子允许 P3 计量换电")
    void freshAllowsMeteredSwap() {
        shadows.save(DeviceShadow.seed("B1", "vendorA", "ext-1", 80, 4200, T0));

        PerformEntitledSwap swap = meteredSwap();
        DomainOutcome<UsageEvent> outcome = swap.execute("U-1", "E-P3", "CAB-1", 80, 60);

        assertInstanceOf(DomainOutcome.Ok.class, outcome);
        assertEquals(1, ledger.findAll().size());
    }

    private PerformEntitledSwap meteredSwap() {
        TelemetryFreshnessPort port =
                new ShadowTelemetryFreshnessAdapter(
                        new AssertShadowFreshForMetered(shadows, CLOCK));
        return new PerformEntitledSwap(
                entitlements, batteries, usages, products, accounts, ledger, CLOCK, port);
    }

    private static final class InMemoryShadows implements DeviceShadowRepository {
        private final Map<String, DeviceShadow> byId = new HashMap<>();

        @Override
        public void save(DeviceShadow shadow) {
            byId.put(shadow.batteryId(), shadow);
        }

        @Override
        public Optional<DeviceShadow> findByBatteryId(String batteryId) {
            return Optional.ofNullable(byId.get(batteryId));
        }
    }

    private static final class InMemoryEntitlements implements EntitlementRepository {
        private final Map<String, Entitlement> byId = new HashMap<>();

        void put(Entitlement e) {
            byId.put(e.id(), e);
        }

        @Override
        public void save(Entitlement entitlement) {
            byId.put(entitlement.id(), entitlement);
        }

        @Override
        public Entitlement get(String entitlementId) {
            return byId.get(entitlementId);
        }

        @Override
        public Optional<Entitlement> findByOrderId(String orderId) {
            return byId.values().stream().filter(e -> e.orderId().equals(orderId)).findFirst();
        }

        @Override
        public List<Entitlement> findActiveByUser(String userId) {
            return byId.values().stream()
                    .filter(e -> e.userId().equals(userId))
                    .filter(e -> e.status() == EntitlementStatus.ACTIVE)
                    .toList();
        }
    }

    private static final class InMemoryBatteries implements BatteryAssetRepository {
        private final Map<String, BatteryAsset> byId = new HashMap<>();

        void put(BatteryAsset b) {
            byId.put(b.id(), b);
        }

        @Override
        public Optional<BatteryAsset> findAnyIdle() {
            return byId.values().stream().filter(BatteryAsset::isIdle).findFirst();
        }

        @Override
        public BatteryAsset get(String batteryId) {
            return byId.get(batteryId);
        }

        @Override
        public void save(BatteryAsset battery) {
            byId.put(battery.id(), battery);
        }
    }

    private static final class InMemoryUsages implements UsageEventRepository {
        private final List<UsageEvent> events = new ArrayList<>();

        List<UsageEvent> all() {
            return List.copyOf(events);
        }

        @Override
        public void save(UsageEvent event) {
            events.removeIf(e -> e.id().equals(event.id()));
            events.add(event);
        }

        @Override
        public Optional<UsageEvent> findStartedByBattery(String batteryId) {
            return events.stream()
                    .filter(e -> e.batteryId().equals(batteryId) && e.isStarted())
                    .findFirst();
        }

        @Override
        public Optional<UsageEvent> findStartedByEntitlement(String entitlementId) {
            return events.stream()
                    .filter(e -> e.entitlementId().equals(entitlementId) && e.isStarted())
                    .findFirst();
        }

        @Override
        public List<UsageEvent> findStartedByUser(String userId) {
            return events.stream()
                    .filter(e -> e.userId().equals(userId) && e.isStarted())
                    .toList();
        }
    }

    private static final class InMemoryProducts implements ProductRepository {
        private final Map<String, Product> byId = new HashMap<>();

        void put(Product p) {
            byId.put(p.id(), p);
        }

        @Override
        public void save(Product product) {
            put(product);
        }

        @Override
        public Product get(String productId) {
            return byId.get(productId);
        }
    }

    private static final class InMemoryAccounts implements AccountRepository {
        private final Map<String, Account> byId = new HashMap<>();

        void put(Account a) {
            byId.put(a.id(), a);
        }

        @Override
        public Account get(String accountId) {
            return byId.get(accountId);
        }

        @Override
        public Account findUserBalance(String userId, Currency currency) {
            return byId.values().stream()
                    .filter(a -> a.ownerType() == AccountOwnerType.USER)
                    .filter(a -> a.ownerId().equals(userId))
                    .filter(a -> a.type() == AccountType.BALANCE)
                    .filter(a -> a.currency() == currency)
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public Account findUserPoints(String userId, Currency currency) {
            return byId.values().stream()
                    .filter(a -> a.ownerType() == AccountOwnerType.USER)
                    .filter(a -> a.ownerId().equals(userId))
                    .filter(a -> a.type() == AccountType.POINTS)
                    .filter(a -> a.currency() == currency)
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public Account findOrgSettlement(String orgId, Currency currency) {
            return byId.values().stream()
                    .filter(a -> a.ownerType() == AccountOwnerType.ORG)
                    .filter(a -> a.ownerId().equals(orgId))
                    .filter(a -> a.type() == AccountType.SETTLEMENT)
                    .filter(a -> a.currency() == currency)
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
}
