package com.evolutionary.iot.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.iot.domain.AlertType;
import com.evolutionary.iot.domain.BatteryAlertRaised;
import com.evolutionary.iot.domain.DeviceShadow;
import com.evolutionary.iot.domain.MaintenanceTicket;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AC-60 / AC-61。 */
class CommLostAndTriageTest {

    private static final Instant T0 = Instant.parse("2026-09-17T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);

    @Test
    @DisplayName("AC-60：lastSeenAt>5min → stale + COMM_LOST + OPEN ticket")
    void staleRaisesCommLostAndTicket() {
        Instant old = T0.minus(DeviceShadow.STALE_AFTER).minusSeconds(30);
        InMemoryShadows shadows = new InMemoryShadows();
        shadows.save(DeviceShadow.seed("B1", "vendorA", "ext-1", 55, 4000, old));
        InMemoryAlerts alerts = new InMemoryAlerts();
        InMemoryTickets tickets = new InMemoryTickets();

        DetectCommLost.Result result =
                new DetectCommLost(shadows, alerts, tickets, CLOCK).execute("B1");

        assertTrue(result.shadow().stale());
        assertTrue(result.raised());
        assertEquals(AlertType.COMM_LOST, result.alert().alertType());
        assertEquals(MaintenanceTicket.Status.OPEN, result.ticket().status());
        assertEquals(1, alerts.findByBatteryId("B1").size());
        assertEquals(1, tickets.findByBatteryId("B1").size());
    }

    @Test
    @DisplayName("AC-60：已有 OPEN COMM_LOST 工单时不重复开单")
    void doesNotDuplicateOpenTicket() {
        Instant old = T0.minus(DeviceShadow.STALE_AFTER).minusSeconds(30);
        InMemoryShadows shadows = new InMemoryShadows();
        shadows.save(DeviceShadow.seed("B1", "vendorA", "ext-1", 55, 4000, old));
        InMemoryAlerts alerts = new InMemoryAlerts();
        InMemoryTickets tickets = new InMemoryTickets();
        DetectCommLost detect = new DetectCommLost(shadows, alerts, tickets, CLOCK);

        DetectCommLost.Result first = detect.execute("B1");
        DetectCommLost.Result second = detect.execute("B1");

        assertEquals(first.ticket().id(), second.ticket().id());
        assertEquals(1, tickets.findByBatteryId("B1").size());
        assertEquals(2, alerts.findByBatteryId("B1").size());
    }

    @Test
    @DisplayName("新鲜影子不告警")
    void freshNoAlert() {
        InMemoryShadows shadows = new InMemoryShadows();
        shadows.save(DeviceShadow.seed("B1", "vendorA", "ext-1", 55, 4000, T0));
        DetectCommLost.Result result =
                new DetectCommLost(shadows, new InMemoryAlerts(), new InMemoryTickets(), CLOCK)
                        .execute("B1");
        assertFalse(result.raised());
        assertFalse(result.shadow().stale());
    }

    @Test
    @DisplayName("AC-61：stale 时下一步是 SHADOW_STALE，检查单先影子后适配器")
    void triageStopsAtStaleShadow() {
        Instant old = T0.minus(DeviceShadow.STALE_AFTER).minusSeconds(1);
        InMemoryShadows shadows = new InMemoryShadows();
        shadows.save(DeviceShadow.seed("B1", "vendorA", "ext-1", 40, 3900, old));

        TriageOutdatedSoc.Report report =
                new TriageOutdatedSoc(shadows, CLOCK).execute("B1");

        assertEquals(TriageOutdatedSoc.NextStep.SHADOW_STALE, report.nextStep());
        assertTrue(report.shadow().stale());
        assertNotNull(report.shadow().lastSeenAt());
        assertEquals("检查 shadow.stale 与 lastSeenAt", report.orderedChecks().get(0));
        assertTrue(report.orderedChecks().get(report.orderedChecks().size() - 1).contains("适配器"));
    }

    @Test
    @DisplayName("AC-61：影子新鲜 → CHECK_ADAPTER")
    void triageFreshGoesToAdapter() {
        InMemoryShadows shadows = new InMemoryShadows();
        shadows.save(DeviceShadow.seed("B1", "vendorA", "ext-1", 40, 3900, T0));

        TriageOutdatedSoc.Report report =
                new TriageOutdatedSoc(shadows, CLOCK).execute("B1");

        assertEquals(TriageOutdatedSoc.NextStep.CHECK_ADAPTER, report.nextStep());
        assertFalse(report.shadow().stale());
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

    private static final class InMemoryAlerts implements AlertStore {
        private final List<BatteryAlertRaised> all = new ArrayList<>();

        @Override
        public void append(BatteryAlertRaised alert) {
            all.add(alert);
        }

        @Override
        public List<BatteryAlertRaised> findByBatteryId(String batteryId) {
            return all.stream().filter(a -> a.batteryId().equals(batteryId)).toList();
        }
    }

    private static final class InMemoryTickets implements MaintenanceTicketRepository {
        private final Map<String, MaintenanceTicket> byId = new HashMap<>();

        @Override
        public void save(MaintenanceTicket ticket) {
            byId.put(ticket.id(), ticket);
        }

        @Override
        public Optional<MaintenanceTicket> findOpen(String batteryId, AlertType alertType) {
            return byId.values().stream()
                    .filter(t -> t.batteryId().equals(batteryId))
                    .filter(t -> t.alertType() == alertType)
                    .filter(MaintenanceTicket::isOpen)
                    .findFirst();
        }

        @Override
        public List<MaintenanceTicket> findByBatteryId(String batteryId) {
            return byId.values().stream().filter(t -> t.batteryId().equals(batteryId)).toList();
        }
    }
}
