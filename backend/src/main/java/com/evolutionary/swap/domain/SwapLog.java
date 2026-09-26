package com.evolutionary.swap.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * 追加式换电日志（审计轨迹）—— 与 {@link SwapSession} 分离：
 * Session 是领域瞬间快照（值对象、无 id）；本记录可持久化、可查询。
 */
public final class SwapLog {

    private final String id;
    private final String stationId;
    private final String outgoingBatteryId;
    private final String incomingBatteryId;
    private final Instant occurredAt;

    private SwapLog(
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

    public static SwapLog of(
            String id,
            String stationId,
            String outgoingBatteryId,
            String incomingBatteryId,
            Instant occurredAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (stationId == null || stationId.isBlank()) {
            throw new IllegalArgumentException("stationId must not be blank");
        }
        Objects.requireNonNull(outgoingBatteryId, "outgoingBatteryId");
        Objects.requireNonNull(incomingBatteryId, "incomingBatteryId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        return new SwapLog(id, stationId, outgoingBatteryId, incomingBatteryId, occurredAt);
    }

    /** 由换电会话生成日志（应用层在唯一写入口调用）。 */
    public static SwapLog fromSession(String id, SwapSession session, Instant occurredAt) {
        Objects.requireNonNull(session, "session");
        return of(
                id,
                session.stationId(),
                session.outgoing().id(),
                session.incoming().id(),
                occurredAt);
    }

    public String id() {
        return id;
    }

    public String stationId() {
        return stationId;
    }

    public String outgoingBatteryId() {
        return outgoingBatteryId;
    }

    public String incomingBatteryId() {
        return incomingBatteryId;
    }

    public Instant occurredAt() {
        return occurredAt;
    }
}
