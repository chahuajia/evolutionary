package com.evolutionary.iot.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryRecordJpaRepository
        extends JpaRepository<TelemetryRecordJpaEntity, String> {

    List<TelemetryRecordJpaEntity> findByBatteryId(String batteryId);
}
