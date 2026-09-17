package com.evolutionary.iot.interfaces;

import com.evolutionary.iot.application.ApplyTelemetryToShadow;
import com.evolutionary.iot.application.DetectCommLost;
import com.evolutionary.iot.application.DeviceShadowRepository;
import com.evolutionary.iot.domain.BatteryTelemetryReported;
import com.evolutionary.iot.domain.DeviceShadow;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IoT HTTP：影子只读 + COMM_LOST 检测开单。
 *
 * <p>领域错误经 {@link IotApiErrorTranslator} 按码翻译（S34）；不嗅探 message。
 */
@RestController
@RequestMapping("/iot")
public class IotController {

    private final DeviceShadowRepository shadows;
    private final DetectCommLost detectCommLost;
    private final ApplyTelemetryToShadow applyTelemetryToShadow;

    public IotController(
            DeviceShadowRepository shadows,
            DetectCommLost detectCommLost,
            ApplyTelemetryToShadow applyTelemetryToShadow) {
        this.shadows = shadows;
        this.detectCommLost = detectCommLost;
        this.applyTelemetryToShadow = applyTelemetryToShadow;
    }

    @GetMapping("/batteries/{batteryId}/shadow")
    public ResponseEntity<ShadowView> shadow(@PathVariable String batteryId) {
        return shadows
                .findByBatteryId(batteryId)
                .map(IotController::toShadow)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/batteries/{batteryId}/telemetry")
    public ResponseEntity<ShadowView> applyTelemetry(
            @PathVariable String batteryId, @RequestBody TelemetryRequest body) {
        if (shadows.findByBatteryId(batteryId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        BatteryTelemetryReported event =
                BatteryTelemetryReported.of(
                        batteryId,
                        body.vendorId(),
                        body.soc(),
                        body.voltageMilli(),
                        Instant.now());
        DeviceShadow updated = applyTelemetryToShadow.execute(event);
        return ResponseEntity.ok(toShadow(updated));
    }

    @PostMapping("/batteries/{batteryId}/detect-comm-lost")
    public ResponseEntity<?> detectCommLost(@PathVariable String batteryId) {
        if (shadows.findByBatteryId(batteryId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DetectCommLost.Result result = detectCommLost.execute(batteryId);
        return ResponseEntity.ok(toDetectView(result));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<IotApiErrorTranslator.ApiError> handleBadRequest(
            IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        return ResponseEntity.badRequest()
                .body(new IotApiErrorTranslator.ApiError(msg, null));
    }

    private static DetectCommLostView toDetectView(DetectCommLost.Result result) {
        DeviceShadow shadow = result.shadow();
        String alertType =
                result.alert() == null ? null : result.alert().alertType().name();
        String ticketId = result.ticket() == null ? null : result.ticket().id();
        return new DetectCommLostView(
                shadow.batteryId(),
                shadow.stale(),
                result.raised(),
                alertType,
                ticketId);
    }

    private static ShadowView toShadow(DeviceShadow s) {
        return new ShadowView(
                s.batteryId(),
                s.vendorId(),
                s.externalDeviceId(),
                s.soc(),
                s.voltageMilli(),
                s.lockState().name(),
                s.status().name(),
                s.lastSeenAt().toString(),
                s.stale(),
                s.updatedAt().toString());
    }

    public record TelemetryRequest(String vendorId, int soc, long voltageMilli) {}

    public record DetectCommLostView(
            String batteryId,
            boolean stale,
            boolean raised,
            String alertType,
            String ticketId) {}

    public record ShadowView(
            String batteryId,
            String vendorId,
            String externalDeviceId,
            int soc,
            long voltageMilli,
            String lockState,
            String status,
            String lastSeenAt,
            boolean stale,
            String updatedAt) {}
}
