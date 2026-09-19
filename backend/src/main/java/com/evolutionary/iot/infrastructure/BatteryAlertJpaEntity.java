package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.domain.AlertType;
import com.evolutionary.iot.domain.BatteryAlertRaised;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 告警追加持久化 —— 只活在 infrastructure。 */
@Entity
@Table(name = "battery_alerts")
public class BatteryAlertJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String batteryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatteryAlertRaised.Severity severity;

    @Column(nullable = false)
    private Instant raisedAt;

    protected BatteryAlertJpaEntity() {}

    public BatteryAlertJpaEntity(
            String id,
            String batteryId,
            AlertType alertType,
            BatteryAlertRaised.Severity severity,
            Instant raisedAt) {
        this.id = id;
        this.batteryId = batteryId;
        this.alertType = alertType;
        this.severity = severity;
        this.raisedAt = raisedAt;
    }

    public String getId() {
        return id;
    }

    public String getBatteryId() {
        return batteryId;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public BatteryAlertRaised.Severity getSeverity() {
        return severity;
    }

    public Instant getRaisedAt() {
        return raisedAt;
    }
}
