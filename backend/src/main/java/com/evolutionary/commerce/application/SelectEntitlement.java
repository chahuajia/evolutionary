package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Entitlement;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 多 Entitlement 选择（phase-1 P1-4 / AC-14）。
 *
 * <p>默认：优先 FINITE 次卡（未用尽）；无可用次卡则用 UNLIMITED。
 */
public final class SelectEntitlement {

    private SelectEntitlement() {}

    public static Optional<Entitlement> selectDefault(List<Entitlement> active, Instant now) {
        Objects.requireNonNull(active, "active");
        Objects.requireNonNull(now, "now");

        List<Entitlement> usable =
                active.stream()
                        .filter(e -> e.isActiveAt(now))
                        .filter(e -> !e.isExhausted())
                        .toList();

        Optional<Entitlement> finite =
                usable.stream().filter(Entitlement::isFinite).findFirst();
        if (finite.isPresent()) {
            return finite;
        }
        return usable.stream().filter(e -> !e.isFinite()).findFirst();
    }
}
