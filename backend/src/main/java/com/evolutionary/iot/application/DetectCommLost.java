package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.AlertType;
import com.evolutionary.iot.domain.BatteryAlertRaised;
import com.evolutionary.iot.domain.DeviceShadow;
import com.evolutionary.iot.domain.MaintenanceTicket;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * stale 检查 → COMM_LOST 告警 + 开运维工单（AC-60）。
 *
 * <p>同电池已有 OPEN+COMM_LOST 工单时不重复开单。
 */
public final class DetectCommLost {

    private final DeviceShadowRepository shadows;
    private final AlertStore alerts;
    private final MaintenanceTicketRepository tickets;
    private final Clock clock;

    public DetectCommLost(
            DeviceShadowRepository shadows,
            AlertStore alerts,
            MaintenanceTicketRepository tickets,
            Clock clock) {
        this.shadows = Objects.requireNonNull(shadows, "shadows");
        this.alerts = Objects.requireNonNull(alerts, "alerts");
        this.tickets = Objects.requireNonNull(tickets, "tickets");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public Result execute(String batteryId) {
        Objects.requireNonNull(batteryId, "batteryId");
        DeviceShadow shadow = shadows.get(batteryId).refreshStale(clock.instant());
        shadows.save(shadow);

        if (!shadow.stale()) {
            return Result.fresh(shadow);
        }

        BatteryAlertRaised alert = BatteryAlertRaised.commLost(batteryId, clock.instant());
        alerts.append(alert);

        MaintenanceTicket ticket =
                tickets.findOpen(batteryId, AlertType.COMM_LOST)
                        .orElseGet(
                                () -> {
                                    MaintenanceTicket opened =
                                            MaintenanceTicket.open(
                                                    "tkt-" + UUID.randomUUID(),
                                                    batteryId,
                                                    AlertType.COMM_LOST,
                                                    clock.instant());
                                    tickets.save(opened);
                                    return opened;
                                });

        return Result.commLost(shadow, alert, ticket);
    }

    public record Result(
            DeviceShadow shadow,
            boolean raised,
            BatteryAlertRaised alert,
            MaintenanceTicket ticket) {

        static Result fresh(DeviceShadow shadow) {
            return new Result(shadow, false, null, null);
        }

        static Result commLost(
                DeviceShadow shadow, BatteryAlertRaised alert, MaintenanceTicket ticket) {
            return new Result(shadow, true, alert, ticket);
        }
    }
}
