package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.BatteryAlertRaised;
import java.util.List;

/** 告警追加存储（阶段 7 内存/端口即可）。 */
public interface AlertStore {
    void append(BatteryAlertRaised alert);

    List<BatteryAlertRaised> findByBatteryId(String batteryId);
}
