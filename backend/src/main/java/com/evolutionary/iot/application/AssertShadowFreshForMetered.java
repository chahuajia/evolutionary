package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.DeviceShadow;
import com.evolutionary.iot.domain.IotErrorCode;
import com.evolutionary.iot.domain.IotOutcome;
import java.time.Clock;
import java.util.Objects;

/**
 * 计量换电前影子新鲜度守卫（AC-58 / INV-19）。
 *
 * <p>业务只读 Shadow，不直连厂商。
 */
public final class AssertShadowFreshForMetered {

    private final DeviceShadowRepository shadows;
    private final Clock clock;

    public AssertShadowFreshForMetered(DeviceShadowRepository shadows, Clock clock) {
        this.shadows = Objects.requireNonNull(shadows, "shadows");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public IotOutcome<DeviceShadow> execute(String batteryId) {
        Objects.requireNonNull(batteryId, "batteryId");
        DeviceShadow shadow = shadows.get(batteryId).refreshStale(clock.instant());
        if (shadow.stale()) {
            return IotOutcome.err(IotErrorCode.TELEMETRY_STALE, "影子过期，禁止按电量计费");
        }
        return IotOutcome.ok(shadow);
    }
}
