package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.application.MaintenanceTicketRepository;
import com.evolutionary.iot.domain.AlertType;
import com.evolutionary.iot.domain.MaintenanceTicket;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 运维工单 JPA 适配（表 maintenance_tickets）。 */
@Component
public final class JpaMaintenanceTicketRepository implements MaintenanceTicketRepository {

    private final MaintenanceTicketJpaRepository jpa;

    public JpaMaintenanceTicketRepository(MaintenanceTicketJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(MaintenanceTicket ticket) {
        jpa.save(
                new MaintenanceTicketJpaEntity(
                        ticket.id(),
                        ticket.batteryId(),
                        ticket.alertType(),
                        ticket.status(),
                        ticket.createdAt()));
    }

    @Override
    public Optional<MaintenanceTicket> findOpen(String batteryId, AlertType alertType) {
        return jpa.findFirstByBatteryIdAndAlertTypeAndStatusOrderByCreatedAtAsc(
                        batteryId, alertType, MaintenanceTicket.Status.OPEN)
                .map(JpaMaintenanceTicketRepository::toDomain);
    }

    @Override
    public List<MaintenanceTicket> findByBatteryId(String batteryId) {
        return jpa.findByBatteryIdOrderByCreatedAtAsc(batteryId).stream()
                .map(JpaMaintenanceTicketRepository::toDomain)
                .toList();
    }

    private static MaintenanceTicket toDomain(MaintenanceTicketJpaEntity row) {
        return MaintenanceTicket.rehydrate(
                row.getId(),
                row.getBatteryId(),
                row.getAlertType(),
                row.getStatus(),
                row.getCreatedAt());
    }
}
