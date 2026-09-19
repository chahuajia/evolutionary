package com.evolutionary.iot.interfaces;

import com.evolutionary.iot.application.ApplyTelemetryToShadow;
import com.evolutionary.iot.application.DetectCommLost;
import com.evolutionary.iot.application.DeviceShadowRepository;
import com.evolutionary.iot.application.MaintenanceTicketRepository;
import com.evolutionary.iot.application.ResolveMaintenanceTicket;
import com.evolutionary.iot.application.TriageOutdatedSoc;
import com.evolutionary.iot.domain.BatteryTelemetryReported;
import com.evolutionary.iot.domain.DeviceShadow;
import com.evolutionary.iot.domain.MaintenanceTicket;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IoT HTTP：影子只读 + COMM_LOST 检测开单 + 遥测入影 + SOC 过时诊断 + 工单列表。
 *
 * <p>领域错误经 {@link IotApiErrorTranslator} 按码翻译（S34）；不嗅探 message。
 */
@RestController
@RequestMapping("/iot")
public class IotController {

    private final DeviceShadowRepository shadows;
    private final DetectCommLost detectCommLost;
    private final ApplyTelemetryToShadow applyTelemetryToShadow;
    private final TriageOutdatedSoc triageOutdatedSoc;
    private final MaintenanceTicketRepository tickets;
    private final ResolveMaintenanceTicket resolveMaintenanceTicket;

    public IotController(
            DeviceShadowRepository shadows,
            DetectCommLost detectCommLost,
            ApplyTelemetryToShadow applyTelemetryToShadow,
            TriageOutdatedSoc triageOutdatedSoc,
            MaintenanceTicketRepository tickets,
            ResolveMaintenanceTicket resolveMaintenanceTicket) {
        this.shadows = shadows;
        this.detectCommLost = detectCommLost;
        this.applyTelemetryToShadow = applyTelemetryToShadow;
        this.triageOutdatedSoc = triageOutdatedSoc;
        this.tickets = tickets;
        this.resolveMaintenanceTicket = resolveMaintenanceTicket;
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

    /** 切片13a / AC-61：SOC 过时诊断（先 shadow.stale，再适配器）。 */
    @PostMapping("/batteries/{batteryId}/triage-outdated-soc")
    public ResponseEntity<?> triageOutdatedSoc(@PathVariable String batteryId) {
        if (shadows.findByBatteryId(batteryId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TriageOutdatedSoc.Report report = triageOutdatedSoc.execute(batteryId);
        return ResponseEntity.ok(toTriageView(report));
    }

    /** 切片15a：按电池列运维工单（detect-comm-lost 开单后可查）。 */
    @GetMapping("/batteries/{batteryId}/tickets")
    public ResponseEntity<?> listTickets(@PathVariable String batteryId) {
        if (shadows.findByBatteryId(batteryId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<TicketView> views =
                tickets.findByBatteryId(batteryId).stream().map(IotController::toTicketView).toList();
        return ResponseEntity.ok(views);
    }

    /** 解决运维工单（对齐 MaintenanceTicket.resolve · 已 RESOLVED 幂等）。 */
    @PostMapping("/tickets/{ticketId}/resolve")
    public ResponseEntity<?> resolveTicket(@PathVariable String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("ticketId required");
        }
        MaintenanceTicket resolved = resolveMaintenanceTicket.execute(ticketId.trim());
        return ResponseEntity.ok(toTicketView(resolved));
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

    private static TriageView toTriageView(TriageOutdatedSoc.Report report) {
        DeviceShadow s = report.shadow();
        return new TriageView(
                s.batteryId(),
                report.nextStep().name(),
                report.orderedChecks(),
                s.soc(),
                s.stale(),
                s.lastSeenAt().toString());
    }

    private static TicketView toTicketView(MaintenanceTicket t) {
        return new TicketView(
                t.id(),
                t.batteryId(),
                t.alertType().name(),
                t.status().name(),
                t.createdAt().toString());
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

    public record TriageView(
            String batteryId,
            String nextStep,
            List<String> orderedChecks,
            int soc,
            boolean stale,
            String lastSeenAt) {}

    public record TicketView(
            String ticketId,
            String batteryId,
            String alertType,
            String status,
            String createdAt) {}

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
