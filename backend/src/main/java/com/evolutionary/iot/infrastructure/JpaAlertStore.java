package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.application.AlertStore;
import com.evolutionary.iot.domain.BatteryAlertRaised;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** 告警追加 JPA 适配（append-only；表 battery_alerts）。 */
@Component
public final class JpaAlertStore implements AlertStore {

    private final BatteryAlertJpaRepository jpa;

    public JpaAlertStore(BatteryAlertJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void append(BatteryAlertRaised alert) {
        jpa.save(toEntity(alert));
    }

    @Override
    public List<BatteryAlertRaised> findByBatteryId(String batteryId) {
        return jpa.findByBatteryId(batteryId).stream().map(JpaAlertStore::toDomain).toList();
    }

    /** 告警领域对象无 id（端口只按 batteryId 查），行主键在 infrastructure 生成。 */
    private static BatteryAlertJpaEntity toEntity(BatteryAlertRaised alert) {
        return new BatteryAlertJpaEntity(
                "alert-" + UUID.randomUUID(),
                alert.batteryId(),
                alert.alertType(),
                alert.severity(),
                alert.raisedAt());
    }

    private static BatteryAlertRaised toDomain(BatteryAlertJpaEntity row) {
        return BatteryAlertRaised.rehydrate(
                row.getBatteryId(), row.getAlertType(), row.getSeverity(), row.getRaisedAt());
    }
}
