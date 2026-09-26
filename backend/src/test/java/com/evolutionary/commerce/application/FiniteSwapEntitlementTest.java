package com.evolutionary.commerce.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.evolutionary.commerce.domain.BatteryAsset;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.commerce.domain.SwapLimit;
import com.evolutionary.commerce.domain.UsageEvent;
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

/** phase-1 切片 1：FINITE 次卡 INV-6/7 · AC-10/11/12 */
class FiniteSwapEntitlementTest {

    private static final Instant T0 = Instant.parse("2026-09-17T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);

    private InMemoryEntitlements entitlements;
    private InMemoryBatteries batteries;
    private InMemoryUsages usages;
    private PerformEntitledSwap swap;

    @BeforeEach
    void setUp() {
        entitlements = new InMemoryEntitlements();
        batteries = new InMemoryBatteries();
        usages = new InMemoryUsages();
        swap = new PerformEntitledSwap(entitlements, batteries, usages, CLOCK);
        batteries.put(BatteryAsset.createIdle("BAT-1", "ORG-1", "v", "m"));
    }

    @Test
    @DisplayName("AC-10：次卡 COMPLETED 后 remainingSwaps -= 1")
    void finiteDecrementsOnComplete() {
        entitlements.put(
                Entitlement.rehydrate(
                        "E-F",
                        "O-1",
                        "U-1",
                        "P-2",
                        T0.minusSeconds(60),
                        T0.plusSeconds(86_400),
                        Entitlement.Status.ACTIVE,
                        10));

        DomainOutcome<UsageEvent> outcome = swap.execute("U-1", "E-F", "CAB-1");

        assertInstanceOf(DomainOutcome.Ok.class, outcome);
        assertEquals(9, entitlements.get("E-F").remainingSwaps());
    }

    @Test
    @DisplayName("AC-11：remainingSwaps=0 拒绝 ENTITLEMENT_EXHAUSTED")
    void exhaustedRejected() {
        entitlements.put(
                Entitlement.rehydrate(
                        "E-0",
                        "O-1",
                        "U-1",
                        "P-2",
                        T0.minusSeconds(60),
                        T0.plusSeconds(86_400),
                        Entitlement.Status.ACTIVE,
                        0));

        DomainOutcome<UsageEvent> outcome = swap.execute("U-1", "E-0", "CAB-1");

        assertInstanceOf(DomainOutcome.Err.class, outcome);
        assertEquals(
                DomainErrorCode.ENTITLEMENT_EXHAUSTED,
                ((DomainOutcome.Err<UsageEvent>) outcome).code());
    }

    @Test
    @DisplayName("AC-12：UNLIMITED 不跟踪 remainingSwaps")
    void unlimitedUnaffected() {
        Product p1 =
                Product.create(
                        "P-1",
                        "ORG-1",
                        "月卡",
                        Money.cny(9900),
                        30,
                        SwapLimit.unlimited(),
                        Product.Status.PUBLISHED);
        Order paid =
                Order.create("O-1", "U-1", "P-1", "ORG-1", Money.cny(9900), T0).pay(T0);
        entitlements.put(Entitlement.createActive("E-U", paid, p1, T0));

        DomainOutcome<UsageEvent> outcome = swap.execute("U-1", "E-U", "CAB-1");

        assertInstanceOf(DomainOutcome.Ok.class, outcome);
        assertNull(entitlements.get("E-U").remainingSwaps());
        assertEquals(Entitlement.Status.ACTIVE, entitlements.get("E-U").status());
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
                    .filter(e -> e.status() == Entitlement.Status.ACTIVE)
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
}
