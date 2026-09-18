package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.application.TelemetryStore;
import com.evolutionary.iot.domain.TelemetryRecord;
import java.util.List;
import org.springframework.stereotype.Component;

/** 遥测时序 JPA 适配（append-only；表 telemetry_records）。 */
@Component
public final class JpaTelemetryStore implements TelemetryStore {

    private final TelemetryRecordJpaRepository jpa;

    public JpaTelemetryStore(TelemetryRecordJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void append(TelemetryRecord record) {
        jpa.save(toEntity(record));
    }

    @Override
    public List<TelemetryRecord> findByBatteryId(String batteryId) {
        return jpa.findByBatteryId(batteryId).stream()
                .map(JpaTelemetryStore::toDomain)
                .toList();
    }

    private static TelemetryRecordJpaEntity toEntity(TelemetryRecord record) {
        return new TelemetryRecordJpaEntity(
                record.id(),
                record.batteryId(),
                record.soc(),
                record.voltageMilli(),
                record.recordedAt());
    }

    private static TelemetryRecord toDomain(TelemetryRecordJpaEntity row) {
        return TelemetryRecord.rehydrate(
                row.getId(),
                row.getBatteryId(),
                row.getSoc(),
                row.getVoltageMv(),
                row.getReportedAt());
    }
}
