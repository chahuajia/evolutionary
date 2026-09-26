package com.evolutionary.swap.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 换电日志持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "swap_logs")
public class SwapLogJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String stationId;

    @Column(nullable = false)
    private String outgoingBatteryId;

    @Column(nullable = false)
    private String incomingBatteryId;

    @Column(nullable = false)
    private Instant occurredAt;

    protected SwapLogJpaEntity() {}

    public SwapLogJpaEntity(
            String id,
            String stationId,
            String outgoingBatteryId,
            String incomingBatteryId,
            Instant occurredAt) {
        this.id = id;
        this.stationId = stationId;
        this.outgoingBatteryId = outgoingBatteryId;
        this.incomingBatteryId = incomingBatteryId;
        this.occurredAt = occurredAt;
    }

    public String getId() {
        return id;
    }

    public String getStationId() {
        return stationId;
    }

    public String getOutgoingBatteryId() {
        return outgoingBatteryId;
    }

    public String getIncomingBatteryId() {
        return incomingBatteryId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
