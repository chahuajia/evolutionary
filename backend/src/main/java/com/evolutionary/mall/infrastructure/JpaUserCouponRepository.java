package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.application.UserCouponRepository;
import com.evolutionary.mall.domain.UserCoupon;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 用户持券 JPA 适配。 */
@Component
public final class JpaUserCouponRepository implements UserCouponRepository {

    private final UserCouponJpaRepository jpa;

    public JpaUserCouponRepository(UserCouponJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<UserCoupon> findById(String userCouponId) {
        return jpa.findById(userCouponId).map(JpaUserCouponRepository::toDomain);
    }

    @Override
    public void save(UserCoupon userCoupon) {
        jpa.save(
                new UserCouponJpaEntity(
                        userCoupon.id(),
                        userCoupon.userId(),
                        userCoupon.templateId(),
                        userCoupon.status(),
                        userCoupon.lockedByOrderId()));
    }

    private static UserCoupon toDomain(UserCouponJpaEntity row) {
        return UserCoupon.rehydrate(
                row.getId(),
                row.getUserId(),
                row.getTemplateId(),
                row.getStatus(),
                row.getLockedByOrderId());
    }
}
