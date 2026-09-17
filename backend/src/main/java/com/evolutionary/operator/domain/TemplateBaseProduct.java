package com.evolutionary.operator.domain;

import java.util.Objects;

/**
 * 模板内嵌的基础商品快照（对照 IDL ProductPhase1 的固定价次卡形状）。
 *
 * <p>不引用 commerce.Product，避免运营域与交易域耦合。
 */
public final class TemplateBaseProduct {

    private final String displayName;
    private final long priceCents;
    private final int durationDays;

    private TemplateBaseProduct(String displayName, long priceCents, int durationDays) {
        this.displayName = displayName;
        this.priceCents = priceCents;
        this.durationDays = durationDays;
    }

    public static TemplateBaseProduct of(String displayName, long priceCents, int durationDays) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName 不能为空");
        }
        if (priceCents < 0) {
            throw new IllegalArgumentException("priceCents 不能为负");
        }
        if (durationDays <= 0) {
            throw new IllegalArgumentException("durationDays 必须为正");
        }
        return new TemplateBaseProduct(displayName, priceCents, durationDays);
    }

    public TemplateBaseProduct withDurationDays(int newDurationDays) {
        return of(displayName, priceCents, newDurationDays);
    }

    public TemplateBaseProduct withPriceCents(long newPriceCents) {
        return of(displayName, newPriceCents, durationDays);
    }

    public TemplateBaseProduct withDisplayName(String newDisplayName) {
        return of(newDisplayName, priceCents, durationDays);
    }

    public String displayName() {
        return displayName;
    }

    public long priceCents() {
        return priceCents;
    }

    public int durationDays() {
        return durationDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TemplateBaseProduct that)) {
            return false;
        }
        return priceCents == that.priceCents
                && durationDays == that.durationDays
                && Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, priceCents, durationDays);
    }
}
