package com.evolutionary.settlement.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 分润规则。
 *
 * <p>约束：显式百分比之和 ≤ 100；余量归 PLATFORM（规格 P4-4）。
 * 推广补贴百分比从 PLATFORM 余量中扣（AC-39）。
 */
public final class ProfitSharingRule {

    public static final String PLATFORM_ORG_ID = "PLATFORM";

    private final String id;
    private final String orgId;
    private final List<ProfitSplit> splits;
    /** 推广补贴百分比；0 表示无补贴。从 PLATFORM 余量扣。 */
    private final int promoterBonusPercent;
    private final Instant effectiveFrom;
    private final Instant effectiveUntil;
    private final int version;

    private ProfitSharingRule(
            String id,
            String orgId,
            List<ProfitSplit> splits,
            int promoterBonusPercent,
            Instant effectiveFrom,
            Instant effectiveUntil,
            int version) {
        this.id = id;
        this.orgId = orgId;
        this.splits = List.copyOf(splits);
        this.promoterBonusPercent = promoterBonusPercent;
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
        return create(id, orgId, splits, 0, effectiveFrom, version);
    }

    public static ProfitSharingRule create(
            String id,
            String orgId,
            List<ProfitSplit> splits,
            int promoterBonusPercent,
            Instant effectiveFrom,
            int version) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id 不能为空");
        }
        Objects.requireNonNull(splits, "splits");
        if (promoterBonusPercent < 0 || promoterBonusPercent > 100) {
            throw new IllegalArgumentException("promoterBonusPercent 须在 0..100");
        }
        int sum = splits.stream().mapToInt(ProfitSplit::percentage).sum();
        if (sum > 100) {
            throw new IllegalArgumentException("显式百分比之和不能超过 100");
        }
        if (sum + promoterBonusPercent > 100) {
            throw new IllegalArgumentException("显式百分比 + 推广补贴不能超过 100");
        }
        return new ProfitSharingRule(
                id,
                requireId(orgId),
                splits,
                promoterBonusPercent,
                Objects.requireNonNull(effectiveFrom, "effectiveFrom"),
                null,
                version);
    }

    /** 持久化回放（infrastructure → domain）。 */
    public static ProfitSharingRule rehydrate(
            String id,
            String orgId,
            List<ProfitSplit> splits,
            int promoterBonusPercent,
            Instant effectiveFrom,
            Instant effectiveUntil,
            int version) {
        return new ProfitSharingRule(
                id,
                orgId,
                Objects.requireNonNull(splits, "splits"),
                promoterBonusPercent,
                Objects.requireNonNull(effectiveFrom, "effectiveFrom"),
                effectiveUntil,
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

    /**
     * 在基础拆分上叠加推广补贴：从 PLATFORM 扣减，记入 promoterOrgId。
     *
     * @param applyBonus false 时（无有效绑定）仅返回基础拆分
     */
    public List<AllocatedShare> allocate(
            long orderAmountCents, String promoterOrgId, boolean applyBonus) {
        List<AllocatedShare> base = allocate(orderAmountCents);
        if (!applyBonus || promoterBonusPercent <= 0) {
            return base;
        }
        if (promoterOrgId == null || promoterOrgId.isBlank()) {
            throw new IllegalArgumentException("promoterOrgId 不能为空");
        }
        long bonusCents = orderAmountCents * promoterBonusPercent / 100;
        if (bonusCents == 0) {
            return base;
        }
        java.util.ArrayList<AllocatedShare> result = new java.util.ArrayList<>();
        boolean platformAdjusted = false;
        for (AllocatedShare share : base) {
            if (PLATFORM_ORG_ID.equals(share.orgId())) {
                long after = share.amountCents() - bonusCents;
                if (after < 0) {
                    throw new IllegalStateException("PLATFORM 余量不足以支付推广补贴");
                }
                if (after > 0) {
                    result.add(new AllocatedShare(PLATFORM_ORG_ID, after));
                }
                platformAdjusted = true;
            } else {
                result.add(share);
            }
        }
        if (!platformAdjusted) {
            throw new IllegalStateException("无 PLATFORM 份额可扣推广补贴");
        }
        result.add(new AllocatedShare(promoterOrgId, bonusCents));
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

    public int promoterBonusPercent() {
        return promoterBonusPercent;
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
