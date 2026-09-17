package com.evolutionary.iot.domain;

import java.time.Instant;
import java.util.Objects;

/** 统一遥测事件（领域只认此形状，INV-18）。 */
public final class BatteryTelemetryReported {

    private final String batteryId;
    private final String vendorId;
    private final int soc;
    private final long voltageMilli;
    private final Instant reportedAt;

    private BatteryTelemetryReported(
            String batteryId, String vendorId, int soc, long voltageMilli, Instant reportedAt) {
        this.batteryId = batteryId;
        this.vendorId = vendorId;
        this.soc = soc;
        this.voltageMilli = voltageMilli;
        this.reportedAt = reportedAt;
    }

    public static BatteryTelemetryReported of(
            String batteryId, String vendorId, int soc, long voltageMilli, Instant reportedAt) {
        if (soc < 0 || soc > 100) {
            throw new IllegalArgumentException("soc 须在 0..100");
        }
        return new BatteryTelemetryReported(
                Objects.requireNonNull(batteryId, "batteryId"),
                Objects.requireNonNull(vendorId, "vendorId"),
                soc,
                voltageMilli,
                Objects.requireNonNull(reportedAt, "reportedAt"));
    }

    public String batteryId() {
        return batteryId;
    }

    public String vendorId() {
        return vendorId;
    }

    public int soc() {
        return soc;
    }

    public long voltageMilli() {
        return voltageMilli;
    }

    public Instant reportedAt() {
        return reportedAt;
    }
}
