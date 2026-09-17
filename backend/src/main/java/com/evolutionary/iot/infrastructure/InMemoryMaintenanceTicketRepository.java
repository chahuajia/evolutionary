package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.application.MaintenanceTicketRepository;
import com.evolutionary.iot.domain.AlertType;
import com.evolutionary.iot.domain.MaintenanceTicket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内运维工单仓储。 */
public final class InMemoryMaintenanceTicketRepository implements MaintenanceTicketRepository {

    private final Map<String, MaintenanceTicket> byId = new ConcurrentHashMap<>();

    @Override
    public void save(MaintenanceTicket ticket) {
        byId.put(ticket.id(), ticket);
    }

    @Override
    public Optional<MaintenanceTicket> findOpen(String batteryId, AlertType alertType) {
        return byId.values().stream()
                .filter(t -> t.batteryId().equals(batteryId))
                .filter(t -> t.alertType() == alertType)
                .filter(MaintenanceTicket::isOpen)
                .findFirst();
    }

    @Override
    public List<MaintenanceTicket> findByBatteryId(String batteryId) {
        List<MaintenanceTicket> matched = new ArrayList<>();
        for (MaintenanceTicket t : byId.values()) {
            if (t.batteryId().equals(batteryId)) {
                matched.add(t);
            }
        }
        return List.copyOf(matched);
    }
}
