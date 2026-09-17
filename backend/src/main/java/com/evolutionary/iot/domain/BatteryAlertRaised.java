package com.evolutionary.iot.domain;

import java.time.Instant;
import java.util.Objects;

/** 统一告警事件。 */
public final class BatteryAlertRaised {

    public enum Severity {
        WARNING,
        CRITICAL
    }

    private final String batteryId;
    private final AlertType alertType;
    private final Severity severity;
    private final Instant raisedAt;

    private BatteryAlertRaised(
            String batteryId, AlertType alertType, Severity severity, Instant raisedAt) {
        this.batteryId = batteryId;
        this.alertType = alertType;
        this.severity = severity;
        this.raisedAt = raisedAt;
    }

    public static BatteryAlertRaised commLost(String batteryId, Instant raisedAt) {
        return new BatteryAlertRaised(
                Objects.requireNonNull(batteryId, "batteryId"),
                AlertType.COMM_LOST,
                Severity.CRITICAL,
                Objects.requireNonNull(raisedAt, "raisedAt"));
    }

    public String batteryId() {
        return batteryId;
    }

    public AlertType alertType() {
        return alertType;
    }

    public Severity severity() {
        return severity;
    }

    public Instant raisedAt() {
        return raisedAt;
    }
}
