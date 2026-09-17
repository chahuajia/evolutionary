package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.UsageEvent;
import java.util.List;
import java.util.Optional;

public interface UsageEventRepository {
    void save(UsageEvent event);

    Optional<UsageEvent> findStartedByBattery(String batteryId);

    List<UsageEvent> findStartedByUser(String userId);
}
