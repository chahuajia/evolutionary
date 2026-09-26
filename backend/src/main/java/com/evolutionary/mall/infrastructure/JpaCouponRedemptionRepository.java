package com.evolutionary.mall.infrastructure;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.application.CouponRedemptionRepository;
import com.evolutionary.mall.domain.CouponRedemption;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 优惠券核销 JPA 适配（append-only）。 */
@Component
public final class JpaCouponRedemptionRepository implements CouponRedemptionRepository {

    private final CouponRedemptionJpaRepository jpa;

    public JpaCouponRedemptionRepository(CouponRedemptionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void append(CouponRedemption redemption) {
        jpa.save(
                new CouponRedemptionJpaEntity(
                        redemption.id(),
                        redemption.userCouponId(),
                        redemption.orderId(),
                        redemption.discountAmount().cents(),
                        redemption.discountAmount().currency(),
                        redemption.redeemedAt()));
    }

    @Override
    public List<CouponRedemption> findByOrderId(String orderId) {
        List<CouponRedemption> result = new ArrayList<>();
        for (CouponRedemptionJpaEntity row : jpa.findByOrderIdOrderByRedeemedAtAsc(orderId)) {
            result.add(toDomain(row));
        }
        return result;
    }

    @Override
    public List<CouponRedemption> findAll() {
        List<CouponRedemption> result = new ArrayList<>();
        for (CouponRedemptionJpaEntity row : jpa.findAll()) {
            result.add(toDomain(row));
        }
        return result;
    }

    private static CouponRedemption toDomain(CouponRedemptionJpaEntity row) {
        return CouponRedemption.rehydrate(
                row.getId(),
                row.getUserCouponId(),
                row.getOrderId(),
                new Money(row.getDiscountAmountCents(), row.getDiscountAmountCurrency()),
                row.getRedeemedAt());
    }
}
