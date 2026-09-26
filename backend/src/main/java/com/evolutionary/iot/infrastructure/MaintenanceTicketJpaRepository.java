package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.domain.AlertType;
import com.evolutionary.iot.domain.MaintenanceTicket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceTicketJpaRepository
        extends JpaRepository<MaintenanceTicketJpaEntity, String> {

    /** findOpen 的落库版本：同电池 + 同告警类型 + 指定状态，取最早一张。 */
    Optional<MaintenanceTicketJpaEntity> findFirstByBatteryIdAndAlertTypeAndStatusOrderByCreatedAtAsc(
            String batteryId, AlertType alertType, MaintenanceTicket.Status status);

    List<MaintenanceTicketJpaEntity> findByBatteryIdOrderByCreatedAtAsc(String batteryId);
}
