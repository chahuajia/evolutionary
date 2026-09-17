package com.evolutionary.iot.application;

import com.evolutionary.commerce.application.TelemetryFreshnessPort;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.iot.domain.DeviceShadow;
import com.evolutionary.iot.domain.IotErrorCode;
import com.evolutionary.iot.domain.IotOutcome;
import java.util.Objects;

/** IoT 影子守卫 → commerce {@link TelemetryFreshnessPort}。 */
public final class ShadowTelemetryFreshnessAdapter implements TelemetryFreshnessPort {

    private final AssertShadowFreshForMetered guard;

    public ShadowTelemetryFreshnessAdapter(AssertShadowFreshForMetered guard) {
        this.guard = Objects.requireNonNull(guard, "guard");
    }

    @Override
    public DomainOutcome<Void> assertFresh(String batteryId) {
        IotOutcome<DeviceShadow> outcome = guard.execute(batteryId);
        if (outcome instanceof IotOutcome.Err<DeviceShadow> err) {
            DomainErrorCode code =
                    err.code() == IotErrorCode.TELEMETRY_STALE
                            ? DomainErrorCode.TELEMETRY_STALE
                            : DomainErrorCode.BATTERY_NOT_AVAILABLE;
            return DomainOutcome.err(code, err.message());
        }
        return DomainOutcome.ok(null);
    }
}
