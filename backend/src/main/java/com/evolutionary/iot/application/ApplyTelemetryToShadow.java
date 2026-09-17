package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.BatteryTelemetryReported;
import com.evolutionary.iot.domain.DeviceShadow;
import com.evolutionary.iot.domain.TelemetryRecord;
import java.time.Clock;
import java.util.Objects;

/**
 * 应用遥测到影子（AC-57）。
 *
 * <p>业务侧只通过本用例读/写 Shadow，不直连厂商（AC-56 / INV-18）。
 */
public final class ApplyTelemetryToShadow {

    private final DeviceShadowRepository shadows;
    private final TelemetryStore telemetry;
    private final Clock clock;

    public ApplyTelemetryToShadow(
            DeviceShadowRepository shadows, TelemetryStore telemetry, Clock clock) {
        this.shadows = Objects.requireNonNull(shadows, "shadows");
        this.telemetry = Objects.requireNonNull(telemetry, "telemetry");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public DeviceShadow execute(BatteryTelemetryReported event) {
        Objects.requireNonNull(event, "event");
        DeviceShadow current = shadows.get(event.batteryId());
        DeviceShadow updated = current.applyTelemetry(event, clock.instant());
        shadows.save(updated);
        telemetry.append(TelemetryRecord.from(event));
        return updated;
    }

    /** 只读取影子供 swap 等业务使用（AC-56）。 */
    public DeviceShadow readForBusiness(String batteryId) {
        return shadows.get(batteryId).refreshStale(clock.instant());
    }
}
