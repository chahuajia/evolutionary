package com.evolutionary.swap.domain;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.battery.domain.BatteryStatus;
import java.util.Objects;

/**
 * 一次换电记录 —— 哪站、换出哪块、换进哪块。
 *
 * <p>值对象：无独立会话 id（规格未要求）。记录的是换电完成瞬间的快照
 * （outgoing = {@code IN_USE}，incoming = {@code CHARGING}）。
 *
 * <p>不变量由本类工厂守（对象自守 —— [[S13]]）；站点库存规则在 {@code Station}。
 */
public final class SwapSession {

    private final String stationId;
    private final Battery outgoing;
    private final Battery incoming;

    private SwapSession(String stationId, Battery outgoing, Battery incoming) {
        this.stationId = stationId;
        this.outgoing = outgoing;
        this.incoming = incoming;
    }

    /**
     * @throws IllegalArgumentException 站 id 空、两块同一身份、或状态不是换电完成态
     */
    public static SwapSession record(String stationId, Battery outgoing, Battery incoming) {
        if (stationId == null || stationId.isBlank()) {
            throw new IllegalArgumentException("station id must not be blank");
        }
        Objects.requireNonNull(outgoing, "outgoing");
        Objects.requireNonNull(incoming, "incoming");
        if (outgoing.id().equals(incoming.id())) {
            throw new IllegalArgumentException("outgoing and incoming must differ");
        }
        if (outgoing.status() != BatteryStatus.IN_USE) {
            throw new IllegalArgumentException("outgoing must be IN_USE");
        }
        if (incoming.status() != BatteryStatus.CHARGING) {
            throw new IllegalArgumentException("incoming must be CHARGING");
        }
        return new SwapSession(stationId, outgoing, incoming);
    }

    public String stationId() {
        return stationId;
    }

    public Battery outgoing() {
        return outgoing;
    }

    public Battery incoming() {
        return incoming;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SwapSession s
                && stationId.equals(s.stationId)
                && outgoing.equals(s.outgoing)
                && incoming.equals(s.incoming);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationId, outgoing, incoming);
    }
}
