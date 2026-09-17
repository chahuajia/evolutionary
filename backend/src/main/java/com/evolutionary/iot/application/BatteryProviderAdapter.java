package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.BatteryTelemetryReported;
import com.evolutionary.iot.domain.ExternalDeviceRef;
import com.evolutionary.iot.domain.ParseError;
import java.util.List;
import java.util.function.Consumer;

/**
 * 厂商适配器端口（P7-1）。
 *
 * <p>{@code raw} 仅在实现类内解析；领域只收 {@link BatteryTelemetryReported}。
 */
public interface BatteryProviderAdapter {

    String vendorId();

    List<ExternalDeviceRef> discover();

    /**
     * @param raw 厂商原始载荷（MQTT 字符串 / HTTP Map 等）
     * @throws ParseError 无法归一化时
     */
    BatteryTelemetryReported parseTelemetry(Object raw) throws ParseError;

    void subscribe(Consumer<BatteryTelemetryReported> callback);
}
