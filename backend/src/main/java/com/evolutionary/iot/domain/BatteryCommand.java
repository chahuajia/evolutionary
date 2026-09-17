package com.evolutionary.iot.domain;

import java.time.Instant;
import java.util.Objects;

/** 下发命令。 */
public final class BatteryCommand {

    public enum Action {
        LOCK,
        UNLOCK,
        RESET
    }

    private final String commandId;
    private final String batteryId;
    private final Action action;
    private final String issuedBy;
    private final Instant issuedAt;

    private BatteryCommand(
            String commandId, String batteryId, Action action, String issuedBy, Instant issuedAt) {
        this.commandId = commandId;
        this.batteryId = batteryId;
        this.action = action;
        this.issuedBy = issuedBy;
        this.issuedAt = issuedAt;
    }

    public static BatteryCommand of(
            String commandId, String batteryId, Action action, String issuedBy, Instant issuedAt) {
        if (commandId == null || commandId.isBlank()) {
            throw new IllegalArgumentException("commandId 不能为空");
        }
        return new BatteryCommand(
                commandId,
                Objects.requireNonNull(batteryId, "batteryId"),
                Objects.requireNonNull(action, "action"),
                Objects.requireNonNull(issuedBy, "issuedBy"),
                Objects.requireNonNull(issuedAt, "issuedAt"));
    }

    public String commandId() {
        return commandId;
    }

    public String batteryId() {
        return batteryId;
    }

    public Action action() {
        return action;
    }

    public String issuedBy() {
        return issuedBy;
    }

    public Instant issuedAt() {
        return issuedAt;
    }
}
