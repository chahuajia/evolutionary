package com.evolutionary.settlement.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * 用户与推广组织的绑定（每用户仅一条 ACTIVE）。
 *
 * <p>绑定后 72 小时内有效下单计入推广（规格 §2）。
 */
public final class ReferralBinding {

    public static final long BINDING_WINDOW_SECONDS = 72L * 3600L;

    private final String userId;
    private final String promoterOrgId;
    private final Instant boundAt;
    private final Instant expiresAt;
    private final ReferralStatus status;

    private ReferralBinding(
            String userId,
            String promoterOrgId,
            Instant boundAt,
            Instant expiresAt,
            ReferralStatus status) {
        this.userId = userId;
        this.promoterOrgId = promoterOrgId;
        this.boundAt = boundAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    /** 持久化回放（infrastructure → domain）。 */
    public static ReferralBinding rehydrate(
            String userId,
            String promoterOrgId,
            Instant boundAt,
            Instant expiresAt,
            ReferralStatus status) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        return new ReferralBinding(
                userId,
                Objects.requireNonNull(promoterOrgId, "promoterOrgId"),
                Objects.requireNonNull(boundAt, "boundAt"),
                Objects.requireNonNull(expiresAt, "expiresAt"),
                Objects.requireNonNull(status, "status"));
    }

    public static ReferralBinding bind(String userId, String promoterOrgId, Instant boundAt) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (promoterOrgId == null || promoterOrgId.isBlank()) {
            throw new IllegalArgumentException("promoterOrgId 不能为空");
        }
        Instant at = Objects.requireNonNull(boundAt, "boundAt");
        return new ReferralBinding(
                userId,
                promoterOrgId,
                at,
                at.plusSeconds(BINDING_WINDOW_SECONDS),
                ReferralStatus.ACTIVE);
    }

    /** 下单时刻是否仍在推广窗口内。 */
    public boolean coversOrderAt(Instant orderAt) {
        return status == ReferralStatus.ACTIVE
                && !orderAt.isBefore(boundAt)
                && orderAt.isBefore(expiresAt);
    }

    public ReferralBinding expire() {
        if (status == ReferralStatus.EXPIRED) {
            return this;
        }
        return new ReferralBinding(userId, promoterOrgId, boundAt, expiresAt, ReferralStatus.EXPIRED);
    }

    public String userId() {
        return userId;
    }

    public String promoterOrgId() {
        return promoterOrgId;
    }

    public Instant boundAt() {
        return boundAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public ReferralStatus status() {
        return status;
    }
}
