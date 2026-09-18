package com.evolutionary.iot.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.iot.application.TelemetryStore;
import com.evolutionary.iot.domain.TelemetryRecord;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片45b：TelemetryStore JPA 落库 telemetry_records。 */
@SpringBootTest
class JpaTelemetryStoreTest {

    private static final Instant T0 = Instant.parse("2026-09-18T05:00:00Z");

    @Autowired
    private TelemetryStore telemetryStore;

    @Autowired
    private TelemetryRecordJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("append → findByBatteryId 命中")
    void appendAndFindByBatteryId() {
        TelemetryRecord record =
                TelemetryRecord.rehydrate("tel-1", "BAT-IOT-1", 42, 3800L, T0);
        telemetryStore.append(record);

        TelemetryRecord found = telemetryStore.findByBatteryId("BAT-IOT-1").get(0);
        assertEquals("tel-1", found.id());
        assertEquals("BAT-IOT-1", found.batteryId());
        assertEquals(42, found.soc());
        assertEquals(3800L, found.voltageMilli());
        assertEquals(T0, found.recordedAt());
        assertTrue(jpa.findById("tel-1").isPresent());
    }

    @Test
    @DisplayName("findByBatteryId 仅返回同电池记录")
    void findByBatteryIdFiltersOtherBatteries() {
        telemetryStore.append(
                TelemetryRecord.rehydrate("tel-A1", "BAT-A", 50, 3700L, T0));
        telemetryStore.append(
                TelemetryRecord.rehydrate("tel-A2", "BAT-A", 51, 3710L, T0.plusSeconds(60)));
        telemetryStore.append(
                TelemetryRecord.rehydrate("tel-B1", "BAT-B", 80, 3900L, T0));

        var forA =
                telemetryStore.findByBatteryId("BAT-A").stream().map(TelemetryRecord::id).sorted()
                        .toList();

        assertEquals(2, forA.size());
        assertEquals("tel-A1", forA.get(0));
        assertEquals("tel-A2", forA.get(1));
        assertEquals(1, telemetryStore.findByBatteryId("BAT-B").size());
    }
}
