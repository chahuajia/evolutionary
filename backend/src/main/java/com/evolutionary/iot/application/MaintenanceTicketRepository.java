package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.AlertType;
import com.evolutionary.iot.domain.MaintenanceTicket;
import java.util.List;
import java.util.Optional;

public interface MaintenanceTicketRepository {
    void save(MaintenanceTicket ticket);

    Optional<MaintenanceTicket> findOpen(String batteryId, AlertType alertType);

    List<MaintenanceTicket> findByBatteryId(String batteryId);
}
