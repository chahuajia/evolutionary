package com.evolutionary.operator.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.operator.application.AuditLogRepository;
import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片34a：AuditLog JPA 落库。 */
@SpringBootTest
class JpaAuditLogRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T08:00:00Z");

    @Autowired
    private AuditLogRepository auditLogs;

    @Autowired
    private AuditLogJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("append → findByResourceId 命中且顺序保持")
    void appendAndFind() {
        auditLogs.append(
                AuditLog.of(
                        "AUD-1",
                        "system",
                        "PLATFORM",
                        AuditAction.COUPON_CLAIM,
                        "UserCoupon",
                        "uc-1",
                        T0));
        auditLogs.append(
                AuditLog.of(
                        "AUD-2",
                        "U-ADMIN",
                        "ORG-PLATFORM",
                        AuditAction.ONBOARDING_APPROVE,
                        "OnboardingApplication",
                        "APP-1",
                        T0.plusSeconds(1)));

        List<AuditLog> byCoupon = auditLogs.findByResourceId("uc-1");
        assertEquals(1, byCoupon.size());
        assertEquals(AuditAction.COUPON_CLAIM, byCoupon.get(0).action());
        assertTrue(jpa.findById("AUD-2").isPresent());
    }
}
