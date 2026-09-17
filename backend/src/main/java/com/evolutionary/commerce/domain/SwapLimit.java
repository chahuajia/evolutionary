package com.evolutionary.commerce.domain;

import java.util.Objects;

/**
 * 换电次数上限。null 语义在 Entitlement 上用 {@code remainingSwaps == null} 表示 UNLIMITED。
 */
public final class SwapLimit {

    private final Integer finite;

    private SwapLimit(Integer finite) {
        this.finite = finite;
    }

    public static SwapLimit unlimited() {
        return new SwapLimit(null);
    }

    public static SwapLimit finite(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("finite swaps must not be negative");
        }
        return new SwapLimit(n);
    }

    public boolean isUnlimited() {
        return finite == null;
    }

    public int remainingOrZero() {
        return finite == null ? 0 : finite;
    }

    public Integer finiteOrNull() {
        return finite;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SwapLimit s && Objects.equals(finite, s.finite);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(finite);
    }
}
