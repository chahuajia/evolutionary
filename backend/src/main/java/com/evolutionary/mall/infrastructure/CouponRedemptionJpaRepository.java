package com.evolutionary.mall.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRedemptionJpaRepository
        extends JpaRepository<CouponRedemptionJpaEntity, String> {

    List<CouponRedemptionJpaEntity> findByOrderIdOrderByRedeemedAtAsc(String orderId);
}
