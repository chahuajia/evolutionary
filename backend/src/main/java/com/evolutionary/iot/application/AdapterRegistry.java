package com.evolutionary.iot.application;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 适配器注册表。 */
public final class AdapterRegistry {

    private final Map<String, BatteryProviderAdapter> byVendor = new ConcurrentHashMap<>();

    public void register(BatteryProviderAdapter adapter) {
        Objects.requireNonNull(adapter, "adapter");
        byVendor.put(adapter.vendorId(), adapter);
    }

    public Optional<BatteryProviderAdapter> find(String vendorId) {
        return Optional.ofNullable(byVendor.get(vendorId));
    }

    public BatteryProviderAdapter get(String vendorId) {
        return find(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("未知厂商适配器: " + vendorId));
    }
}
