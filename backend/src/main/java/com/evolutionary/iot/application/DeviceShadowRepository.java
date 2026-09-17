package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.DeviceShadow;
import java.util.Optional;

public interface DeviceShadowRepository {
    void save(DeviceShadow shadow);

    Optional<DeviceShadow> findByBatteryId(String batteryId);

    default DeviceShadow get(String batteryId) {
        return findByBatteryId(batteryId)
                .orElseThrow(() -> new IllegalArgumentException("未知影子: " + batteryId));
    }
}
