package com.evolutionary.iot.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 遥测历史持久化 —— 只活在 infrastructure。 */
@Entity
@Table(name = "telemetry_records")
public class TelemetryRecordJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String batteryId;

    @Column(nullable = false)
    private int soc;

    @Column(nullable = false)
    private long voltageMv;

    @Column(nullable = false)
    private Instant reportedAt;

    protected TelemetryRecordJpaEntity() {}

    public TelemetryRecordJpaEntity(
            String id, String batteryId, int soc, long voltageMv, Instant reportedAt) {
        this.id = id;
        this.batteryId = batteryId;
        this.soc = soc;
        this.voltageMv = voltageMv;
        this.reportedAt = reportedAt;
    }

    public String getId() {
        return id;
    }

    public String getBatteryId() {
        return batteryId;
    }

    public int getSoc() {
        return soc;
    }

    public long getVoltageMv() {
        return voltageMv;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }
}
