package com.evolutionary.credit.domain;

import com.evolutionary.commerce.domain.Money;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** 信用政策版本（P6-6）；降额仅影响新购。 */
public final class CreditPolicy {

    private final String id;
    private final int version;
    private final Map<ScoreTier, Money> tierLimits;
    private final Instant effectiveFrom;

    private CreditPolicy(
            String id, int version, Map<ScoreTier, Money> tierLimits, Instant effectiveFrom) {
        this.id = id;
        this.version = version;
        this.tierLimits = Map.copyOf(tierLimits);
        this.effectiveFrom = effectiveFrom;
    }

    public static CreditPolicy of(
            String id, int version, Map<ScoreTier, Money> tierLimits, Instant effectiveFrom) {
        if (version < 1) {
            throw new IllegalArgumentException("version 须 ≥ 1");
        }
        Objects.requireNonNull(tierLimits, "tierLimits");
        for (ScoreTier t : ScoreTier.values()) {
            if (!tierLimits.containsKey(t)) {
                throw new IllegalArgumentException("缺少档位限额: " + t);
            }
        }
        return new CreditPolicy(
                Objects.requireNonNull(id, "id"),
                version,
                tierLimits,
                Objects.requireNonNull(effectiveFrom, "effectiveFrom"));
    }

    public Money limitFor(ScoreTier tier) {
        return tierLimits.get(tier);
    }

    public String id() {
        return id;
    }

    public int version() {
        return version;
    }

    public Map<ScoreTier, Money> tierLimits() {
        return tierLimits;
    }

    public Instant effectiveFrom() {
        return effectiveFrom;
    }
}
