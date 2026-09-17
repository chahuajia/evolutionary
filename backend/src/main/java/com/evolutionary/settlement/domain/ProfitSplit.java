package com.evolutionary.settlement.domain;

import java.util.Objects;

/** 分润拆分项：某 org 拿百分比。 */
public final class ProfitSplit {

    private final String orgId;
    private final int percentage;

    private ProfitSplit(String orgId, int percentage) {
        this.orgId = orgId;
        this.percentage = percentage;
    }

    public static ProfitSplit of(String orgId, int percentage) {
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId 不能为空");
        }
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("percentage 须在 0..100");
        }
        return new ProfitSplit(orgId, percentage);
    }

    public String orgId() {
        return orgId;
    }

    public int percentage() {
        return percentage;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ProfitSplit s
                && percentage == s.percentage
                && Objects.equals(orgId, s.orgId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgId, percentage);
    }
}
