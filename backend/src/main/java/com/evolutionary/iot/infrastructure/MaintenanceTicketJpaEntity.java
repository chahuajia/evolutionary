package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.domain.AlertType;
import com.evolutionary.iot.domain.MaintenanceTicket;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 运维工单持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "maintenance_tickets")
public class MaintenanceTicketJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String batteryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceTicket.Status status;

    @Column(nullable = false)
    private Instant createdAt;

    protected MaintenanceTicketJpaEntity() {}

    public MaintenanceTicketJpaEntity(
            String id,
            String batteryId,
            AlertType alertType,
            MaintenanceTicket.Status status,
            Instant createdAt) {
        this.id = id;
        this.batteryId = batteryId;
        this.alertType = alertType;
        this.status = status;
        this.createdAt = createdAt;
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

    public MaintenanceTicket.Status getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
