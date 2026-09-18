package com.evolutionary.mall.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.mall.application.UserCouponRepository;
import com.evolutionary.mall.domain.MallOutcome;
import com.evolutionary.mall.domain.UserCoupon;
import com.evolutionary.mall.domain.UserCouponStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片41b：UserCoupon JPA 落库 user_coupons。 */
@SpringBootTest
class JpaUserCouponRepositoryTest {

    @Autowired
    private UserCouponRepository userCoupons;

    @Autowired
    private UserCouponJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("issue → save → findById")
    void saveAndFindById() {
        UserCoupon issued = UserCoupon.issue("UC-JPA-1", "U1", "T-C1");
        userCoupons.save(issued);

        UserCoupon found = userCoupons.findById("UC-JPA-1").orElseThrow();
        assertEquals("UC-JPA-1", found.id());
        assertEquals("U1", found.userId());
        assertEquals("T-C1", found.templateId());
        assertEquals(UserCouponStatus.AVAILABLE, found.status());
        assertTrue(jpa.findById("UC-JPA-1").isPresent());
    }

    @Test
    @DisplayName("lock 后再 save 验证 status/lockedByOrderId")
    void lockThenSave() {
        UserCoupon issued = UserCoupon.issue("UC-JPA-2", "U1", "T-C1");
        userCoupons.save(issued);

        MallOutcome<UserCoupon> locked = issued.lock("ORDER-1");
        assertInstanceOf(MallOutcome.Ok.class, locked);
        UserCoupon after = ((MallOutcome.Ok<UserCoupon>) locked).value();
        userCoupons.save(after);

        UserCoupon found = userCoupons.findById("UC-JPA-2").orElseThrow();
        assertEquals(UserCouponStatus.LOCKED, found.status());
        assertEquals("ORDER-1", found.lockedByOrderId());
        assertEquals(
                UserCouponStatus.LOCKED,
                jpa.findById("UC-JPA-2").orElseThrow().getStatus());
        assertEquals("ORDER-1", jpa.findById("UC-JPA-2").orElseThrow().getLockedByOrderId());
    }
}
