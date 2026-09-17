package com.evolutionary.commerce.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.evolutionary.commerce.domain.BatteryAsset;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.EntitlementStatus;
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

/** phase-1 切片 2：多 Entitlement 默认策略 AC-14 */
class DefaultEntitlementSelectTest {

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
    @DisplayName("AC-14：P1 UNLIMITED + P2 FINITE 同时 ACTIVE，默认扣 P2")
    void defaultPrefersFiniteOverUnlimited() {
        entitlements.put(
                Entitlement.rehydrate(
                        "E-P1",
                        "O-P1",
                        "U-1",
                        "P-1",
                        T0.minusSeconds(60),
                        T0.plusSeconds(86_400),
                        EntitlementStatus.ACTIVE,
                        null));
        entitlements.put(
                Entitlement.rehydrate(
                        "E-P2",
                        "O-P2",
                        "U-1",
                        "P-2",
                        T0.minusSeconds(60),
                        T0.plusSeconds(86_400),
                        EntitlementStatus.ACTIVE,
                        3));

        DomainOutcome<UsageEvent> outcome = swap.executeWithoutId("U-1", "CAB-1");

        assertInstanceOf(DomainOutcome.Ok.class, outcome);
        UsageEvent event = ((DomainOutcome.Ok<UsageEvent>) outcome).value();
        assertEquals("E-P2", event.entitlementId());
        assertEquals(2, entitlements.get("E-P2").remainingSwaps());
        assertNull(entitlements.get("E-P1").remainingSwaps());
        assertEquals(EntitlementStatus.ACTIVE, entitlements.get("E-P1").status());
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
