package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.application.CouponRedemptionRepository;
import com.evolutionary.mall.domain.CouponRedemption;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 进程内优惠券核销仓储（append-only）。 */
public final class InMemoryCouponRedemptionRepository implements CouponRedemptionRepository {

    private final List<CouponRedemption> all = new CopyOnWriteArrayList<>();

    @Override
    public void append(CouponRedemption redemption) {
        all.add(redemption);
    }

    @Override
    public List<CouponRedemption> findByOrderId(String orderId) {
        return all.stream().filter(r -> r.orderId().equals(orderId)).toList();
    }

    @Override
    public List<CouponRedemption> findAll() {
        return List.copyOf(all);
    }
}
