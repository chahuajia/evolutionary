package com.evolutionary.iot.domain;

import java.time.Instant;
import java.util.Objects;

/** 命令回执。 */
public final class BatteryCommandAcked {

    private final String commandId;
    private final String batteryId;
    private final BatteryCommand.Action action;
    private final boolean success;
    private final Instant ackedAt;

    private BatteryCommandAcked(
            String commandId,
            String batteryId,
            BatteryCommand.Action action,
            boolean success,
            Instant ackedAt) {
        this.commandId = commandId;
        this.batteryId = batteryId;
        this.action = action;
        this.success = success;
        this.ackedAt = ackedAt;
    }

    public static BatteryCommandAcked of(
            String commandId,
            String batteryId,
            BatteryCommand.Action action,
            boolean success,
            Instant ackedAt) {
        return new BatteryCommandAcked(
                Objects.requireNonNull(commandId, "commandId"),
                Objects.requireNonNull(batteryId, "batteryId"),
                Objects.requireNonNull(action, "action"),
                success,
                Objects.requireNonNull(ackedAt, "ackedAt"));
    }

    public String commandId() {
        return commandId;
    }

    public String batteryId() {
        return batteryId;
    }

    public BatteryCommand.Action action() {
        return action;
    }

    public boolean success() {
        return success;
    }

    public Instant ackedAt() {
        return ackedAt;
    }
}
