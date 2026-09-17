package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.application.UserCouponRepository;
import com.evolutionary.mall.domain.UserCoupon;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryUserCouponRepository implements UserCouponRepository {

    private final Map<String, UserCoupon> byId = new ConcurrentHashMap<>();

    @Override
    public Optional<UserCoupon> findById(String userCouponId) {
        return Optional.ofNullable(byId.get(userCouponId));
    }

    @Override
    public void save(UserCoupon userCoupon) {
        byId.put(userCoupon.id(), userCoupon);
    }
}
