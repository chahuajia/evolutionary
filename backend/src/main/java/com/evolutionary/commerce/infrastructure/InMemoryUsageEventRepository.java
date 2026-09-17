package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.UsageEventRepository;
import com.evolutionary.commerce.domain.UsageEvent;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内用量事件仓储（非计量换电路径最小实现）。 */
public final class InMemoryUsageEventRepository implements UsageEventRepository {

    private final Map<String, UsageEvent> byId = new ConcurrentHashMap<>();

    @Override
    public void save(UsageEvent event) {
        byId.put(event.id(), event);
    }

    @Override
    public Optional<UsageEvent> findStartedByBattery(String batteryId) {
        return byId.values().stream()
                .filter(e -> e.batteryId().equals(batteryId) && e.isStarted())
                .findFirst();
    }

    @Override
    public List<UsageEvent> findStartedByUser(String userId) {
        return byId.values().stream()
                .filter(e -> e.userId().equals(userId) && e.isStarted())
                .toList();
    }

    @Override
    public Optional<UsageEvent> findStartedByEntitlement(String entitlementId) {
        return byId.values().stream()
                .filter(e -> e.entitlementId().equals(entitlementId) && e.isStarted())
                .findFirst();
    }
}
