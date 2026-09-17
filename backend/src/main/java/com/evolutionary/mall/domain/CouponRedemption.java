package com.evolutionary.mall.domain;

import com.evolutionary.commerce.domain.Money;
import java.time.Instant;
import java.util.Objects;

/** 优惠券核销记录（append-only，P5-5）。 */
public final class CouponRedemption {

    private final String id;
    private final String userCouponId;
    private final String orderId;
    private final Money discountAmount;
    private final Instant redeemedAt;

    private CouponRedemption(
            String id,
            String userCouponId,
            String orderId,
            Money discountAmount,
            Instant redeemedAt) {
        this.id = id;
        this.userCouponId = userCouponId;
        this.orderId = orderId;
        this.discountAmount = discountAmount;
        this.redeemedAt = redeemedAt;
    }

    public static CouponRedemption record(
            String id,
            String userCouponId,
            String orderId,
            Money discountAmount,
            Instant redeemedAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("redemption id 不能为空");
        }
        if (userCouponId == null || userCouponId.isBlank()) {
            throw new IllegalArgumentException("userCouponId 不能为空");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        return new CouponRedemption(
                id,
                userCouponId,
                orderId,
                Objects.requireNonNull(discountAmount, "discountAmount"),
                Objects.requireNonNull(redeemedAt, "redeemedAt"));
    }

    public String id() {
        return id;
    }

    public String userCouponId() {
        return userCouponId;
    }

    public String orderId() {
        return orderId;
    }

    public Money discountAmount() {
        return discountAmount;
    }

    public Instant redeemedAt() {
        return redeemedAt;
    }
}
