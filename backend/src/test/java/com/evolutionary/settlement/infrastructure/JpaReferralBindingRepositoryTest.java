package com.evolutionary.settlement.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.settlement.application.ReferralBindingRepository;
import com.evolutionary.settlement.domain.ReferralBinding;
import com.evolutionary.settlement.domain.ReferralStatus;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片43b：ReferralBinding JPA 落库。 */
@SpringBootTest
class JpaReferralBindingRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T04:00:00Z");

    @Autowired
    private ReferralBindingRepository bindings;

    @Autowired
    private ReferralBindingJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("bind → save → findActiveByUserId 命中")
    void bindSaveAndFindActiveByUserId() {
        bindings.save(ReferralBinding.bind("U-T", "ORG-L2", T0));

        ReferralBinding found = bindings.findActiveByUserId("U-T").orElseThrow();
        assertEquals("U-T", found.userId());
        assertEquals("ORG-L2", found.promoterOrgId());
        assertEquals(T0, found.boundAt());
        assertEquals(T0.plusSeconds(ReferralBinding.BINDING_WINDOW_SECONDS), found.expiresAt());
        assertEquals(ReferralStatus.ACTIVE, found.status());
        assertTrue(jpa.findById("U-T").isPresent());
    }

    @Test
    @DisplayName("expire 后再 save → findActiveByUserId 未命中")
    void expireThenFindActiveByUserIdEmpty() {
        ReferralBinding active = ReferralBinding.bind("U-T2", "ORG-L1", T0);
        bindings.save(active.expire());

        assertTrue(bindings.findActiveByUserId("U-T2").isEmpty());
        assertEquals(ReferralStatus.EXPIRED, jpa.findById("U-T2").orElseThrow().getStatus());
    }
}
