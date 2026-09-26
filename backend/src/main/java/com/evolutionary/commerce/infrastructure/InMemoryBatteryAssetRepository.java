package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.BatteryAssetRepository;
import com.evolutionary.commerce.domain.BatteryAsset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内电池台账（单测用；生产由 {@link JpaBatteryAssetRepository} 接管）。 */
public final class InMemoryBatteryAssetRepository implements BatteryAssetRepository {

    private final Map<String, BatteryAsset> byId = new ConcurrentHashMap<>();

    @Override
    public Optional<BatteryAsset> findAnyIdle() {
        return byId.values().stream().filter(BatteryAsset::isIdle).findFirst();
    }

    @Override
    public BatteryAsset get(String batteryId) {
        BatteryAsset b = byId.get(batteryId);
        if (b == null) {
            throw new IllegalArgumentException("unknown battery");
        }
        return b;
    }

    @Override
    public void save(BatteryAsset battery) {
        byId.put(battery.id(), battery);
    }
}
