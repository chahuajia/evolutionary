package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.application.DeviceShadowRepository;
import com.evolutionary.iot.domain.DeviceShadow;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内设备影子仓储。 */
public final class InMemoryDeviceShadowRepository implements DeviceShadowRepository {

    private final Map<String, DeviceShadow> byBatteryId = new ConcurrentHashMap<>();

    @Override
    public void save(DeviceShadow shadow) {
        byBatteryId.put(shadow.batteryId(), shadow);
    }

    @Override
    public Optional<DeviceShadow> findByBatteryId(String batteryId) {
        return Optional.ofNullable(byBatteryId.get(batteryId));
    }
}
