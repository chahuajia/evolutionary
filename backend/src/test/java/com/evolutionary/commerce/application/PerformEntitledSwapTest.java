package com.evolutionary.commerce.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.evolutionary.commerce.domain.BatteryAsset;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
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

class PerformEntitledSwapTest {

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

        entitlements.put(
                Entitlement.rehydrate(
                        "E-1",
                        "O-1",
                        "U-1",
                        "P-1",
                        T0.minusSeconds(3600),
                        T0.plusSeconds(86_400),
                        Entitlement.Status.ACTIVE));
        batteries.put(BatteryAsset.createIdle("BAT-1", "ORG-1", "vendor", "model"));
    }

    @Test
    @DisplayName("有效权益换电成功：事件 COMPLETED，电池回到 IDLE")
    void successfulSwap() {
        DomainOutcome<UsageEvent> outcome = swap.execute("U-1", "E-1", "CAB-1");

        assertInstanceOf(DomainOutcome.Ok.class, outcome);
        UsageEvent event = ((DomainOutcome.Ok<UsageEvent>) outcome).value();
        assertEquals(UsageEvent.Status.COMPLETED, event.status());
        assertEquals(BatteryAsset.Status.IDLE, batteries.get("BAT-1").status());
    }

    @Test
    @DisplayName("过期权益拒绝换电")
    void expiredEntitlement() {
        entitlements.put(
                Entitlement.rehydrate(
                        "E-2",
                        "O-1",
                        "U-1",
                        "P-1",
                        T0.minusSeconds(10_000),
                        T0.minusSeconds(1),
                        Entitlement.Status.ACTIVE));

        DomainOutcome<UsageEvent> outcome = swap.execute("U-1", "E-2", "CAB-1");

        assertInstanceOf(DomainOutcome.Err.class, outcome);
        assertEquals(
                DomainErrorCode.ENTITLEMENT_EXPIRED,
                ((DomainOutcome.Err<UsageEvent>) outcome).code());
    }

    @Test
    @DisplayName("同电池已有 STARTED 时拒绝（INV-3）")
    void inv3BlocksSecondStart() {
        usages.save(UsageEvent.start("UE-open", "U-1", "E-1", "BAT-1", "CAB-0", T0));

        DomainOutcome<UsageEvent> outcome = swap.executeWithBattery("U-1", "E-1", "CAB-1", "BAT-1");

        assertInstanceOf(DomainOutcome.Err.class, outcome);
        assertEquals(
                DomainErrorCode.BATTERY_ALREADY_RENTED,
                ((DomainOutcome.Err<UsageEvent>) outcome).code());
    }

    @Test
    @DisplayName("无空闲电池时拒绝")
    void noIdleBattery() {
        batteries.put(
                BatteryAsset.createIdle("BAT-1", "ORG-1", "v", "m").checkout("U-9"));

        DomainOutcome<UsageEvent> outcome = swap.execute("U-1", "E-1", "CAB-1");

        assertInstanceOf(DomainOutcome.Err.class, outcome);
        assertEquals(
                DomainErrorCode.BATTERY_NOT_AVAILABLE,
                ((DomainOutcome.Err<UsageEvent>) outcome).code());
    }

    private static final class InMemoryEntitlements implements EntitlementRepository {
        private final Map<String, Entitlement> byId = new HashMap<>();

        void put(Entitlement entitlement) {
            byId.put(entitlement.id(), entitlement);
        }

        @Override
        public void save(Entitlement entitlement) {
            byId.put(entitlement.id(), entitlement);
        }

        @Override
        public Entitlement get(String entitlementId) {
            Entitlement e = byId.get(entitlementId);
            if (e == null) {
                throw new IllegalArgumentException("unknown entitlement");
            }
            return e;
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

        void put(BatteryAsset battery) {
            byId.put(battery.id(), battery);
        }

        @Override
        public Optional<BatteryAsset> findAnyIdle() {
            return byId.values().stream().filter(BatteryAsset::isIdle).findFirst();
        }

        @Override
        public BatteryAsset get(String batteryId) {
            BatteryAsset b = byId.get(batteryId);
            if (b == null) {
                throw new IllegalArgumentException("unknown battery");
            }
            return b;
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
        public List<UsageEvent> findStartedByUser(String userId) {
            return events.stream()
                    .filter(e -> e.userId().equals(userId) && e.isStarted())
                    .toList();
        }

        @Override
        public Optional<UsageEvent> findStartedByEntitlement(String entitlementId) {
            return events.stream()
                    .filter(e -> e.entitlementId().equals(entitlementId) && e.isStarted())
                    .findFirst();
        }
    }
}
