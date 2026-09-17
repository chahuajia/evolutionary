package com.evolutionary.mall.application;

import com.evolutionary.mall.domain.CouponRedemption;
import java.util.List;

public interface CouponRedemptionRepository {
    /** append-only。 */
    void append(CouponRedemption redemption);

    List<CouponRedemption> findByOrderId(String orderId);

    List<CouponRedemption> findAll();
}
