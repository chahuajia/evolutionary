package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.BatteryAsset;
import java.util.Optional;

public interface BatteryAssetRepository {
    Optional<BatteryAsset> findAnyIdle();

    BatteryAsset get(String batteryId);

    void save(BatteryAsset battery);
}
