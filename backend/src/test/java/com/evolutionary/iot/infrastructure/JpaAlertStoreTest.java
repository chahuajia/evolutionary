package com.evolutionary.iot.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.iot.application.AlertStore;
import com.evolutionary.iot.domain.AlertType;
import com.evolutionary.iot.domain.BatteryAlertRaised;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片46a：AlertStore JPA 落库 battery_alerts。 */
@SpringBootTest
class JpaAlertStoreTest {

    private static final Instant T0 = Instant.parse("2026-09-18T06:00:00Z");

    @Autowired
    private AlertStore alerts;

    @Autowired
    private BatteryAlertJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("append → findByBatteryId 命中且字段完整回放")
    void appendAndFindByBatteryId() {
        alerts.append(BatteryAlertRaised.commLost("BAT-IOT-1", T0));

        BatteryAlertRaised found = alerts.findByBatteryId("BAT-IOT-1").get(0);
        assertEquals("BAT-IOT-1", found.batteryId());
        assertEquals(AlertType.COMM_LOST, found.alertType());
        assertEquals(BatteryAlertRaised.Severity.CRITICAL, found.severity());
        assertEquals(T0, found.raisedAt());
        assertEquals(1, jpa.count());
    }

    @Test
    @DisplayName("findByBatteryId 仅返回同电池记录；重复 append 不去重")
    void findByBatteryIdFiltersOtherBatteriesAndKeepsDuplicates() {
        alerts.append(BatteryAlertRaised.commLost("BAT-A", T0));
        alerts.append(BatteryAlertRaised.commLost("BAT-A", T0.plusSeconds(60)));
        alerts.append(BatteryAlertRaised.commLost("BAT-B", T0));

        assertEquals(2, alerts.findByBatteryId("BAT-A").size());
        assertEquals(1, alerts.findByBatteryId("BAT-B").size());
        assertTrue(alerts.findByBatteryId("BAT-MISSING").isEmpty());
        assertEquals(3, jpa.count());
    }

    @Test
    @DisplayName("rehydrate 缺 batteryId → NullPointerException")
    void rehydrateRejectsMissingBatteryId() {
        assertThrows(
                NullPointerException.class,
                () ->
                        BatteryAlertRaised.rehydrate(
                                null,
                                AlertType.COMM_LOST,
                                BatteryAlertRaised.Severity.CRITICAL,
                                T0));
    }
}
