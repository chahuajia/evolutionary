package com.evolutionary.iot.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.iot.domain.BatteryTelemetryReported;
import com.evolutionary.iot.domain.DeviceShadow;
import com.evolutionary.iot.domain.TelemetryRecord;
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

/** AC-56 / AC-57。 */
class DeviceShadowTest {

    private static final Instant T0 = Instant.parse("2026-09-17T08:00:00Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);

    private InMemoryShadows shadows;
    private InMemoryTelemetry telemetry;
    private ApplyTelemetryToShadow apply;

    @BeforeEach
    void setUp() {
        shadows = new InMemoryShadows();
        telemetry = new InMemoryTelemetry();
        apply = new ApplyTelemetryToShadow(shadows, telemetry, CLOCK);
        shadows.save(
                DeviceShadow.seed("B1", "vendorA", "ext-a-1", 80, 4200, T0.minusSeconds(60)));
    }

    @Test
    @DisplayName("AC-57：遥测更新 soc，刷新 lastSeenAt，stale=false，追加 TelemetryRecord")
    void telemetryUpdatesShadow() {
        BatteryTelemetryReported event =
                BatteryTelemetryReported.of("B1", "vendorA", 75, 4150, T0);
        DeviceShadow updated = apply.execute(event);

        assertEquals(75, updated.soc());
        assertEquals(4150, updated.voltageMilli());
        assertEquals(T0, updated.lastSeenAt());
        assertFalse(updated.stale());
        assertEquals(1, telemetry.findByBatteryId("B1").size());
        assertEquals(75, telemetry.findByBatteryId("B1").get(0).soc());
    }

    @Test
    @DisplayName("AC-56：业务只读 Shadow，超时后 stale=true")
    void businessReadMarksStale() {
        Instant late = T0.plus(DeviceShadow.STALE_AFTER).plusSeconds(1);
        ApplyTelemetryToShadow lateApply =
                new ApplyTelemetryToShadow(
                        shadows, telemetry, Clock.fixed(late, ZoneOffset.UTC));
        DeviceShadow viewed = lateApply.readForBusiness("B1");
        assertTrue(viewed.stale());
        assertEquals(80, viewed.soc());
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

    private static final class InMemoryTelemetry implements TelemetryStore {
        private final List<TelemetryRecord> records = new ArrayList<>();

        @Override
        public void append(TelemetryRecord record) {
            records.add(record);
        }

        @Override
        public List<TelemetryRecord> findByBatteryId(String batteryId) {
            return records.stream().filter(r -> r.batteryId().equals(batteryId)).toList();
        }
    }
}
