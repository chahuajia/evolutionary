package com.evolutionary.mall.application;

import com.evolutionary.mall.domain.UserCoupon;
import java.util.Optional;

public interface UserCouponRepository {
    Optional<UserCoupon> findById(String userCouponId);

    void save(UserCoupon userCoupon);
}
