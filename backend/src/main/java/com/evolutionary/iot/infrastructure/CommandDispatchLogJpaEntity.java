package com.evolutionary.iot.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import com.evolutionary.iot.domain.BatteryCommand;

/** 命令下发审计持久化 —— 只活在 infrastructure。 */
@Entity
@Table(name = "command_dispatch_logs")
public class CommandDispatchLogJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String commandId;

    @Column(nullable = false)
    private String batteryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatteryCommand.Action action;

    @Column(nullable = false)
    private boolean ack;

    @Column(nullable = false)
    private Instant occurredAt;

    protected CommandDispatchLogJpaEntity() {}

    public CommandDispatchLogJpaEntity(
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

    public String getId() {
        return id;
    }

    public String getCommandId() {
        return commandId;
    }

    public String getBatteryId() {
        return batteryId;
    }

    public BatteryCommand.Action getAction() {
        return action;
    }

    public boolean isAck() {
        return ack;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
