package com.evolutionary.mall.domain;

import com.evolutionary.commerce.domain.Money;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 优惠券模板（P5-3）。
 *
 * <p>同 {@code mutexGroup} 互斥；叠加张数上限由规则引擎强制 ≤2。
 */
public final class CouponTemplate {

    private final String id;
    private final String issuerOrgId;
    private final IssuerType issuerType;
    private final CouponKind kind;
    /** FIXED_OFF：分；PERCENT_OFF：减免百分点。 */
    private final long value;
    private final Money minSpend;
    private final CouponScope scope;
    private final List<String> scopeIds;
    private final String mutexGroup;
    private final String campaignId;
    private final Instant validFrom;
    private final Instant validUntil;
    private final Integer perUserLimit;

    private CouponTemplate(
            String id,
            String issuerOrgId,
            IssuerType issuerType,
            CouponKind kind,
            long value,
            Money minSpend,
            CouponScope scope,
            List<String> scopeIds,
            String mutexGroup,
            String campaignId,
            Instant validFrom,
            Instant validUntil,
            Integer perUserLimit) {
        this.id = id;
        this.issuerOrgId = issuerOrgId;
        this.issuerType = issuerType;
        this.kind = kind;
        this.value = value;
        this.minSpend = minSpend;
        this.scope = scope;
        this.scopeIds = scopeIds == null ? List.of() : List.copyOf(scopeIds);
        this.mutexGroup = mutexGroup;
        this.campaignId = campaignId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.perUserLimit = perUserLimit;
    }

    public static CouponTemplate create(
            String id,
            String issuerOrgId,
            IssuerType issuerType,
            CouponKind kind,
            long value,
            Money minSpend,
            CouponScope scope,
            List<String> scopeIds,
            String mutexGroup,
            String campaignId,
            Instant validFrom,
            Instant validUntil) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("template id 不能为空");
        }
        if (issuerOrgId == null || issuerOrgId.isBlank()) {
            throw new IllegalArgumentException("issuerOrgId 不能为空");
        }
        if (mutexGroup == null || mutexGroup.isBlank()) {
            throw new IllegalArgumentException("mutexGroup 不能为空");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("value 必须为正");
        }
        if (kind == CouponKind.PERCENT_OFF && value > 100) {
            throw new IllegalArgumentException("百分比减免不得超过 100");
        }
        Objects.requireNonNull(issuerType, "issuerType");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(validFrom, "validFrom");
        Objects.requireNonNull(validUntil, "validUntil");
        if (!validUntil.isAfter(validFrom)) {
            throw new IllegalArgumentException("validUntil 必须晚于 validFrom");
        }
        return new CouponTemplate(
                id,
                issuerOrgId,
                issuerType,
                kind,
                value,
                minSpend,
                scope,
                scopeIds,
                mutexGroup,
                campaignId,
                validFrom,
                validUntil,
                null);
    }

    /** 领券时占用的面值预算（FIXED_OFF=value；PERCENT_OFF 按面值估算用 value 作为分）。 */
    public Money faceBudget() {
        return Money.cny(value);
    }

    /** 相对订单小计计算单券减免额（已假定 minSpend / scope 通过）。 */
    public Money computeDiscount(Money orderTotal) {
        Objects.requireNonNull(orderTotal, "orderTotal");
        long raw =
                switch (kind) {
                    case FIXED_OFF -> value;
                    case PERCENT_OFF -> orderTotal.cents() * value / 100;
                };
        long capped = Math.min(raw, orderTotal.cents());
        return new Money(capped, orderTotal.currency());
    }

    public boolean meetsMinSpend(Money orderTotal) {
        if (minSpend == null) {
            return true;
        }
        return orderTotal.cents() >= minSpend.cents();
    }

    public Optional<Money> minSpend() {
        return Optional.ofNullable(minSpend);
    }

    public String id() {
        return id;
    }

    public String issuerOrgId() {
        return issuerOrgId;
    }

    public IssuerType issuerType() {
        return issuerType;
    }

    public CouponKind kind() {
        return kind;
    }

    public long value() {
        return value;
    }

    public CouponScope scope() {
        return scope;
    }

    public List<String> scopeIds() {
        return scopeIds;
    }

    public String mutexGroup() {
        return mutexGroup;
    }

    public String campaignId() {
        return campaignId;
    }

    public Instant validFrom() {
        return validFrom;
    }

    public Instant validUntil() {
        return validUntil;
    }

    public Integer perUserLimit() {
        return perUserLimit;
    }
}
