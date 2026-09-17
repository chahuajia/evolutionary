package com.evolutionary.settlement.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 分润规则。
 *
 * <p>约束：显式百分比之和 ≤ 100；余量归 PLATFORM（规格 P4-4）。
 */
public final class ProfitSharingRule {

    public static final String PLATFORM_ORG_ID = "PLATFORM";

    private final String id;
    private final String orgId;
    private final List<ProfitSplit> splits;
    private final Instant effectiveFrom;
    private final Instant effectiveUntil;
    private final int version;

    private ProfitSharingRule(
            String id,
            String orgId,
            List<ProfitSplit> splits,
            Instant effectiveFrom,
            Instant effectiveUntil,
            int version) {
        this.id = id;
        this.orgId = orgId;
        this.splits = List.copyOf(splits);
        this.effectiveFrom = effectiveFrom;
        this.effectiveUntil = effectiveUntil;
        this.version = version;
    }

    public static ProfitSharingRule create(
            String id,
            String orgId,
            List<ProfitSplit> splits,
            Instant effectiveFrom,
            int version) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id 不能为空");
        }
        Objects.requireNonNull(splits, "splits");
        int sum = splits.stream().mapToInt(ProfitSplit::percentage).sum();
        if (sum > 100) {
            throw new IllegalArgumentException("显式百分比之和不能超过 100");
        }
        return new ProfitSharingRule(
                id,
                requireId(orgId),
                splits,
                Objects.requireNonNull(effectiveFrom, "effectiveFrom"),
                null,
                version);
    }

    /** 按订单金额拆分；余量给 PLATFORM。 */
    public List<AllocatedShare> allocate(long orderAmountCents) {
        if (orderAmountCents < 0) {
            throw new IllegalArgumentException("金额不能为负");
        }
        java.util.ArrayList<AllocatedShare> result = new java.util.ArrayList<>();
        long allocated = 0;
        for (ProfitSplit split : splits) {
            long amount = orderAmountCents * split.percentage() / 100;
            result.add(new AllocatedShare(split.orgId(), amount));
            allocated += amount;
        }
        long remainder = orderAmountCents - allocated;
        if (remainder > 0) {
            result.add(new AllocatedShare(PLATFORM_ORG_ID, remainder));
        }
        return List.copyOf(result);
    }

    public String id() {
        return id;
    }

    public String orgId() {
        return orgId;
    }

    public List<ProfitSplit> splits() {
        return splits;
    }

    public Instant effectiveFrom() {
        return effectiveFrom;
    }

    public Instant effectiveUntil() {
        return effectiveUntil;
    }

    public int version() {
        return version;
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id 不能为空");
        }
        return id;
    }

    /** 分配结果。 */
    public record AllocatedShare(String orgId, long amountCents) {}
}
