package com.evolutionary.iot.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 命令下发审计（append-only）。与幂等缓存分离：物理下发成功/失败各记一条。
 */
public final class CommandDispatchLog {

    private final String id;
    private final String commandId;
    private final String batteryId;
    private final BatteryCommand.Action action;
    private final boolean ack;
    private final Instant occurredAt;

    private CommandDispatchLog(
            String id,
            String commandId,
            String batteryId,
            BatteryCommand.Action action,
            boolean ack,
            Instant occurredAt) {
        this.id = id;
        this.commandId = commandId;
        this.batteryId = batteryId;
        this.action = action;
        this.ack = ack;
        this.occurredAt = occurredAt;
    }

    public static CommandDispatchLog of(
            String commandId,
            String batteryId,
            BatteryCommand.Action action,
            boolean ack,
            Instant occurredAt) {
        if (commandId == null || commandId.isBlank()) {
            throw new IllegalArgumentException("commandId 不能为空");
        }
        return new CommandDispatchLog(
                "cdl-" + UUID.randomUUID(),
                commandId,
                batteryId,
                Objects.requireNonNull(action, "action"),
                ack,
                Objects.requireNonNull(occurredAt, "occurredAt"));
    }

    public static CommandDispatchLog fromAck(BatteryCommandAcked ack) {
        return of(
                ack.commandId(),
                ack.batteryId(),
                ack.action(),
                ack.success(),
                ack.ackedAt());
    }

    public String id() {
        return id;
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

    public boolean ack() {
        return ack;
    }

    public Instant occurredAt() {
        return occurredAt;
    }
}
