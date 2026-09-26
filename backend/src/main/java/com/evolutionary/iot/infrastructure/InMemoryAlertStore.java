package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.application.AlertStore;
import com.evolutionary.iot.domain.BatteryAlertRaised;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 进程内告警追加存储。 */
public final class InMemoryAlertStore implements AlertStore {

    private final List<BatteryAlertRaised> all = new CopyOnWriteArrayList<>();

    @Override
    public void append(BatteryAlertRaised alert) {
        all.add(alert);
    }

    @Override
    public List<BatteryAlertRaised> findByBatteryId(String batteryId) {
        List<BatteryAlertRaised> matched = new ArrayList<>();
        for (BatteryAlertRaised a : all) {
            if (a.batteryId().equals(batteryId)) {
                matched.add(a);
            }
        }
        return List.copyOf(matched);
    }
}
