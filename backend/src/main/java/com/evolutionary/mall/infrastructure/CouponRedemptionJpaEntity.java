package com.evolutionary.mall.infrastructure;

import com.evolutionary.commerce.domain.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "coupon_redemptions")
public class CouponRedemptionJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userCouponId;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private long discountAmountCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency discountAmountCurrency;

    @Column(nullable = false)
    private Instant redeemedAt;

    protected CouponRedemptionJpaEntity() {}

    public CouponRedemptionJpaEntity(
            String id,
            String userCouponId,
            String orderId,
            long discountAmountCents,
            Currency discountAmountCurrency,
            Instant redeemedAt) {
        this.id = id;
        this.userCouponId = userCouponId;
        this.orderId = orderId;
        this.discountAmountCents = discountAmountCents;
        this.discountAmountCurrency = discountAmountCurrency;
        this.redeemedAt = redeemedAt;
    }

    public String getId() {
        return id;
    }

    public String getUserCouponId() {
        return userCouponId;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getDiscountAmountCents() {
        return discountAmountCents;
    }

    public Currency getDiscountAmountCurrency() {
        return discountAmountCurrency;
    }

    public Instant getRedeemedAt() {
        return redeemedAt;
    }
}
