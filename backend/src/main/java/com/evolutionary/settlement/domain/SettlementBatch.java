package com.evolutionary.settlement.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * 分润结算批（规格 §5）。
 *
 * <p>阶段 4 简化：不跑 T+7 定时器，由应用显式 {@code runBatch} 关账。
 */
public final class SettlementBatch {

    private final String id;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final BatchStatus status;
    private final Instant createdAt;
    private final Instant closedAt;

    private SettlementBatch(
            String id,
            Instant periodStart,
            Instant periodEnd,
            BatchStatus status,
            Instant createdAt,
            Instant closedAt) {
        this.id = id;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = status;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

    /** 持久化回放（infrastructure → domain）。 */
    public static SettlementBatch rehydrate(
            String id,
            Instant periodStart,
            Instant periodEnd,
            BatchStatus status,
            Instant createdAt,
            Instant closedAt) {
        return new SettlementBatch(
                requireId(id),
                Objects.requireNonNull(periodStart, "periodStart"),
                Objects.requireNonNull(periodEnd, "periodEnd"),
                Objects.requireNonNull(status, "status"),
                Objects.requireNonNull(createdAt, "createdAt"),
                closedAt);
    }

    public static SettlementBatch open(
            String id, Instant periodStart, Instant periodEnd, Instant createdAt) {
        Instant start = Objects.requireNonNull(periodStart, "periodStart");
        Instant end = Objects.requireNonNull(periodEnd, "periodEnd");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("periodEnd 必须晚于 periodStart");
        }
        return new SettlementBatch(
                requireId(id),
                start,
                end,
                BatchStatus.OPEN,
                Objects.requireNonNull(createdAt, "createdAt"),
                null);
    }

    /** 关账：OPEN → CLOSED。 */
    public SettlementBatch close(Instant closedAt) {
        if (status != BatchStatus.OPEN) {
            throw new IllegalStateException("仅 OPEN 批可关账");
        }
        return new SettlementBatch(
                id,
                periodStart,
                periodEnd,
                BatchStatus.CLOSED,
                createdAt,
                Objects.requireNonNull(closedAt, "closedAt"));
    }

    public String id() {
        return id;
    }

    public Instant periodStart() {
        return periodStart;
    }

    public Instant periodEnd() {
        return periodEnd;
    }

    public BatchStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant closedAt() {
        return closedAt;
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id 不能为空");
        }
        return id;
    }
}
