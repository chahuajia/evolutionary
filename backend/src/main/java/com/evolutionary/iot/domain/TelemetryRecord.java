package com.evolutionary.iot.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** 遥测历史记录（append-only；存储实现可后换）。 */
public final class TelemetryRecord {

    private final String id;
    private final String batteryId;
    private final int soc;
    private final long voltageMilli;
    private final Instant recordedAt;

    private TelemetryRecord(
            String id, String batteryId, int soc, long voltageMilli, Instant recordedAt) {
        this.id = id;
        this.batteryId = batteryId;
        this.soc = soc;
        this.voltageMilli = voltageMilli;
        this.recordedAt = recordedAt;
    }

    public static TelemetryRecord from(BatteryTelemetryReported event) {
        return new TelemetryRecord(
                "tel-" + UUID.randomUUID(),
                event.batteryId(),
                event.soc(),
                event.voltageMilli(),
                event.reportedAt());
    }

    public String id() {
        return id;
    }

    public String batteryId() {
        return batteryId;
    }

    public int soc() {
        return soc;
    }

    public long voltageMilli() {
        return voltageMilli;
    }

    public Instant recordedAt() {
        return recordedAt;
    }
}
