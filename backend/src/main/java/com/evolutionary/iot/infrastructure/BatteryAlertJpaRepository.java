package com.evolutionary.iot.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatteryAlertJpaRepository extends JpaRepository<BatteryAlertJpaEntity, String> {

    List<BatteryAlertJpaEntity> findByBatteryId(String batteryId);
}
