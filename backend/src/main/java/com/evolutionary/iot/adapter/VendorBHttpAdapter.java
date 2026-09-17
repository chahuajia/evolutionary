package com.evolutionary.iot.adapter;

import com.evolutionary.iot.application.BatteryProviderAdapter;
import com.evolutionary.iot.domain.BatteryTelemetryReported;
import com.evolutionary.iot.domain.ExternalDeviceRef;
import com.evolutionary.iot.domain.ParseError;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * VendorB：模拟 HTTP JSON Map {@code {deviceId, socPercent, voltageMv}}。
 */
public final class VendorBHttpAdapter implements BatteryProviderAdapter {

    public static final String VENDOR_ID = "vendorB";

    private final List<Consumer<BatteryTelemetryReported>> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, String> externalToBattery;

    public VendorBHttpAdapter(Map<String, String> externalToBattery) {
        this.externalToBattery = Map.copyOf(Objects.requireNonNull(externalToBattery));
    }

    @Override
    public String vendorId() {
        return VENDOR_ID;
    }

    @Override
    public List<ExternalDeviceRef> discover() {
        return externalToBattery.keySet().stream()
                .map(ext -> new ExternalDeviceRef(ext, VENDOR_ID))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public BatteryTelemetryReported parseTelemetry(Object raw) throws ParseError {
        if (!(raw instanceof Map<?, ?> map)) {
            throw new ParseError("VendorB 期望 JSON Map 载荷");
        }
        Object deviceId = map.get("deviceId");
        Object socPercent = map.get("socPercent");
        Object voltageMv = map.get("voltageMv");
        if (deviceId == null || socPercent == null || voltageMv == null) {
            throw new ParseError("VendorB 缺少必填字段");
        }
        String ext = String.valueOf(deviceId);
        String batteryId = externalToBattery.get(ext);
        if (batteryId == null) {
            throw new ParseError("未知外部设备: " + ext);
        }
        int soc = ((Number) socPercent).intValue();
        long voltage = ((Number) voltageMv).longValue();
        BatteryTelemetryReported event =
                BatteryTelemetryReported.of(batteryId, VENDOR_ID, soc, voltage, Instant.now());
        listeners.forEach(l -> l.accept(event));
        return event;
    }

    @Override
    public void subscribe(Consumer<BatteryTelemetryReported> callback) {
        listeners.add(Objects.requireNonNull(callback));
    }
}
