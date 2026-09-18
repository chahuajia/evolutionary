package com.evolutionary.mall.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.application.CouponRedemptionRepository;
import com.evolutionary.mall.domain.CouponRedemption;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片42b：CouponRedemption JPA 落库 coupon_redemptions。 */
@SpringBootTest
class JpaCouponRedemptionRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T04:00:00Z");

    @Autowired
    private CouponRedemptionRepository redemptions;

    @Autowired
    private CouponRedemptionJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("append → findByOrderId 命中")
    void appendAndFindByOrderId() {
        redemptions.append(
                CouponRedemption.record("CR-1", "UC-1", "O-1", Money.cny(500), T0));

        List<CouponRedemption> byOrder = redemptions.findByOrderId("O-1");
        assertEquals(1, byOrder.size());
        assertEquals("CR-1", byOrder.get(0).id());
        assertEquals("UC-1", byOrder.get(0).userCouponId());
        assertEquals(500, byOrder.get(0).discountAmount().cents());
        assertEquals(T0, byOrder.get(0).redeemedAt());

        assertEquals(1, redemptions.findAll().size());
        assertTrue(jpa.findById("CR-1").isPresent());
    }
}
