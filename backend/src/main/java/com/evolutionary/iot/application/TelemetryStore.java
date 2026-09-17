package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.TelemetryRecord;
import java.util.List;

/** 时序端口（阶段 7 不绑具体库）。 */
public interface TelemetryStore {
    void append(TelemetryRecord record);

    List<TelemetryRecord> findByBatteryId(String batteryId);
}
