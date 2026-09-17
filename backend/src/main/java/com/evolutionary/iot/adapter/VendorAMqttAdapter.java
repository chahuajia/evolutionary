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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VendorA：模拟 MQTT 文本载荷 {@code bat=<id>;soc=<n>;v=<mV>}。
 */
public final class VendorAMqttAdapter implements BatteryProviderAdapter {

    public static final String VENDOR_ID = "vendorA";

    private static final Pattern PAYLOAD =
            Pattern.compile("bat=([^;]+);soc=(\\d+);v=(\\d+)");

    private final List<Consumer<BatteryTelemetryReported>> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, String> externalToBattery;

    public VendorAMqttAdapter(Map<String, String> externalToBattery) {
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
    public BatteryTelemetryReported parseTelemetry(Object raw) throws ParseError {
        if (!(raw instanceof String text)) {
            throw new ParseError("VendorA 期望 MQTT 字符串载荷");
        }
        Matcher m = PAYLOAD.matcher(text.trim());
        if (!m.matches()) {
            throw new ParseError("VendorA 载荷格式错误: " + text);
        }
        String ext = m.group(1);
        String batteryId = externalToBattery.get(ext);
        if (batteryId == null) {
            throw new ParseError("未知外部设备: " + ext);
        }
        int soc = Integer.parseInt(m.group(2));
        long voltage = Long.parseLong(m.group(3));
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
