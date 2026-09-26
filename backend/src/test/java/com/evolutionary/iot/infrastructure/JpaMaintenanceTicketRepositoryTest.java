package com.evolutionary.iot.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.iot.application.MaintenanceTicketRepository;
import com.evolutionary.iot.domain.AlertType;
import com.evolutionary.iot.domain.MaintenanceTicket;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片46b：MaintenanceTicket JPA 落库。 */
@SpringBootTest
class JpaMaintenanceTicketRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T04:00:00Z");

    @Autowired private MaintenanceTicketRepository tickets;

    @Autowired private MaintenanceTicketJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("open → save → findOpen 命中，findByBatteryId 可见")
    void saveOpenAndFind() {
        tickets.save(MaintenanceTicket.open("tkt-1", "BAT-T1", AlertType.COMM_LOST, T0));

        MaintenanceTicket found = tickets.findOpen("BAT-T1", AlertType.COMM_LOST).orElseThrow();
        assertEquals("tkt-1", found.id());
        assertEquals("BAT-T1", found.batteryId());
        assertEquals(AlertType.COMM_LOST, found.alertType());
        assertEquals(MaintenanceTicket.Status.OPEN, found.status());
        assertEquals(T0, found.createdAt());

        assertEquals(List.of("tkt-1"), tickets.findByBatteryId("BAT-T1").stream().map(MaintenanceTicket::id).toList());
        assertTrue(jpa.findById("tkt-1").isPresent());
    }

    @Test
    @DisplayName("resolve 后 → findOpen 落空，findByBatteryId 仍可见")
    void resolvedNotOpen() {
        MaintenanceTicket open = MaintenanceTicket.open("tkt-2", "BAT-T2", AlertType.COMM_LOST, T0);
        tickets.save(open);
        tickets.save(open.resolve());

        assertTrue(tickets.findOpen("BAT-T2", AlertType.COMM_LOST).isEmpty());

        MaintenanceTicket reloaded = tickets.findByBatteryId("BAT-T2").get(0);
        assertEquals(MaintenanceTicket.Status.RESOLVED, reloaded.status());
    }

    @Test
    @DisplayName("告警类型/电池不匹配 → findOpen 落空")
    void findOpenRespectsBatteryAndAlertType() {
        tickets.save(MaintenanceTicket.open("tkt-3", "BAT-T3", AlertType.COMM_LOST, T0));

        assertTrue(tickets.findOpen("BAT-T3", AlertType.OVERHEAT).isEmpty());
        assertTrue(tickets.findOpen("BAT-OTHER", AlertType.COMM_LOST).isEmpty());
        assertTrue(tickets.findByBatteryId("BAT-OTHER").isEmpty());
    }
}
