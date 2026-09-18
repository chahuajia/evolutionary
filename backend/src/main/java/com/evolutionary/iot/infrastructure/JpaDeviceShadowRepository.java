package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.application.DeviceShadowRepository;
import com.evolutionary.iot.domain.DeviceShadow;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 设备影子 JPA 适配（表 device_shadows）。 */
@Component
public final class JpaDeviceShadowRepository implements DeviceShadowRepository {

    private final DeviceShadowJpaRepository jpa;

    public JpaDeviceShadowRepository(DeviceShadowJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(DeviceShadow shadow) {
        jpa.save(
                new DeviceShadowJpaEntity(
                        shadow.batteryId(),
                        shadow.vendorId(),
                        shadow.externalDeviceId(),
                        shadow.soc(),
                        shadow.voltageMilli(),
                        shadow.lastSeenAt()));
    }

    @Override
    public Optional<DeviceShadow> findByBatteryId(String batteryId) {
        return jpa.findById(batteryId).map(JpaDeviceShadowRepository::toDomain);
    }

    private static DeviceShadow toDomain(DeviceShadowJpaEntity row) {
        return DeviceShadow.rehydrate(
                row.getBatteryId(),
                row.getVendorId(),
                row.getExternalId(),
                row.getSoc(),
                row.getVoltageMv(),
                row.getLastSeen());
    }
}
