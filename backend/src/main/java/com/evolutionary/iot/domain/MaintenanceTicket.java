package com.evolutionary.iot.domain;

import java.time.Instant;
import java.util.Objects;

/** 运维工单（简化）。 */
public final class MaintenanceTicket {

    public enum Status {
        OPEN,
        RESOLVED
    }

    private final String id;
    private final String batteryId;
    private final AlertType alertType;
    private final Status status;
    private final Instant createdAt;

    private MaintenanceTicket(
            String id, String batteryId, AlertType alertType, Status status, Instant createdAt) {
        this.id = id;
        this.batteryId = batteryId;
        this.alertType = alertType;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static MaintenanceTicket open(
            String id, String batteryId, AlertType alertType, Instant createdAt) {
        return new MaintenanceTicket(
                Objects.requireNonNull(id, "id"),
                Objects.requireNonNull(batteryId, "batteryId"),
                Objects.requireNonNull(alertType, "alertType"),
                Status.OPEN,
                Objects.requireNonNull(createdAt, "createdAt"));
    }

    public MaintenanceTicket resolve() {
        if (status == Status.RESOLVED) {
            return this;
        }
        return new MaintenanceTicket(id, batteryId, alertType, Status.RESOLVED, createdAt);
    }

    public String id() {
        return id;
    }

    public String batteryId() {
        return batteryId;
    }

    public AlertType alertType() {
        return alertType;
    }

    public Status status() {
        return status;
    }

    public boolean isOpen() {
        return status == Status.OPEN;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
