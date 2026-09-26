package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.application.TelemetryStore;
import com.evolutionary.iot.domain.TelemetryRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 进程内遥测时序存储（阶段 7 端口实现）。 */
public final class InMemoryTelemetryStore implements TelemetryStore {

    private final List<TelemetryRecord> records = new CopyOnWriteArrayList<>();

    @Override
    public void append(TelemetryRecord record) {
        records.add(record);
    }

    @Override
    public List<TelemetryRecord> findByBatteryId(String batteryId) {
        List<TelemetryRecord> matched = new ArrayList<>();
        for (TelemetryRecord r : records) {
            if (r.batteryId().equals(batteryId)) {
                matched.add(r);
            }
        }
        return List.copyOf(matched);
    }
}
