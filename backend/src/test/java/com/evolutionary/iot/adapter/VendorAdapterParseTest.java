package com.evolutionary.iot.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.evolutionary.iot.application.AdapterRegistry;
import com.evolutionary.iot.domain.BatteryTelemetryReported;
import com.evolutionary.iot.domain.ParseError;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AC-55：双厂商 raw → 同一事件形状。 */
class VendorAdapterParseTest {

    private VendorAMqttAdapter vendorA;
    private VendorBHttpAdapter vendorB;
    private AdapterRegistry registry;

    @BeforeEach
    void setUp() {
        vendorA = new VendorAMqttAdapter(Map.of("ext-a-1", "B1"));
        vendorB = new VendorBHttpAdapter(Map.of("ext-b-1", "B1"));
        registry = new AdapterRegistry();
        registry.register(vendorA);
        registry.register(vendorB);
    }

    @Test
    @DisplayName("AC-55：VendorA MQTT 与 VendorB HTTP 产出同形 BatteryTelemetryReported")
    void bothVendorsNormalizeToSameShape() {
        BatteryTelemetryReported fromA =
                vendorA.parseTelemetry("bat=ext-a-1;soc=80;v=4200");
        BatteryTelemetryReported fromB =
                vendorB.parseTelemetry(
                        Map.of("deviceId", "ext-b-1", "socPercent", 80, "voltageMv", 4200));

        assertEquals("B1", fromA.batteryId());
        assertEquals("B1", fromB.batteryId());
        assertEquals(80, fromA.soc());
        assertEquals(80, fromB.soc());
        assertEquals(4200, fromA.voltageMilli());
        assertEquals(4200, fromB.voltageMilli());
        assertEquals("vendorA", fromA.vendorId());
        assertEquals("vendorB", fromB.vendorId());
        assertEquals(vendorA, registry.get("vendorA"));
    }

    @Test
    @DisplayName("非法载荷 → ParseError，不泄漏到领域对象")
    void badPayloadRejected() {
        assertThrows(ParseError.class, () -> vendorA.parseTelemetry("{bad}"));
        assertThrows(ParseError.class, () -> vendorB.parseTelemetry("not-a-map"));
    }
}
