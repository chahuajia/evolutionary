package com.evolutionary.commerce.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.EntitlementStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片35a：Entitlement JPA 落库。 */
@SpringBootTest
class JpaEntitlementRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T04:00:00Z");

    @Autowired
    private EntitlementRepository entitlements;

    @Autowired
    private EntitlementJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("save ACTIVE → findActiveByUser 命中")
    void saveAndFindActiveByUser() {
        entitlements.save(
                Entitlement.rehydrate(
                        "E-JPA-1",
                        "O-JPA-1",
                        "U-JPA",
                        "P-1",
                        T0.minusSeconds(3600),
                        T0.plusSeconds(86_400),
                        EntitlementStatus.ACTIVE));

        List<Entitlement> found = entitlements.findActiveByUser("U-JPA");
        assertEquals(1, found.size());
        assertEquals("E-JPA-1", found.get(0).id());
        assertEquals(EntitlementStatus.ACTIVE, found.get(0).status());
        assertTrue(jpa.findById("E-JPA-1").isPresent());
    }
}
