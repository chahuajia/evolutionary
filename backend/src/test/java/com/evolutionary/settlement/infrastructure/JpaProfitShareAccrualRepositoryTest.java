package com.evolutionary.settlement.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.settlement.application.ProfitShareAccrualRepository;
import com.evolutionary.settlement.domain.ProfitShareAccrual;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片44a：ProfitShareAccrual JPA 落库 profit_share_accruals。 */
@SpringBootTest
class JpaProfitShareAccrualRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T04:00:00Z");

    @Autowired
    private ProfitShareAccrualRepository accruals;

    @Autowired
    private ProfitShareAccrualJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("pending → save → findByOrderId 命中")
    void pendingSaveAndFindByOrderId() {
        ProfitShareAccrual pending =
                ProfitShareAccrual.pending(
                        "A-1", "O-T", "ORG-L2", 500L, "CNY", 1, T0);
        accruals.save(pending);

        ProfitShareAccrual found = accruals.findByOrderId("O-T").get(0);
        assertEquals("A-1", found.id());
        assertEquals("O-T", found.orderId());
        assertEquals("ORG-L2", found.orgId());
        assertEquals(500L, found.amountCents());
        assertEquals("CNY", found.currency());
        assertEquals(1, found.ruleVersion());
        assertEquals(ProfitShareAccrual.Status.PENDING, found.status());
        assertEquals(T0, found.createdAt());
        assertTrue(jpa.findById("A-1").isPresent());
    }

    @Test
    @DisplayName("findPendingCreatedBetween 周期过滤（含 start，不含 end）")
    void findPendingCreatedBetweenPeriodFilter() {
        Instant periodStart = T0;
        Instant periodEnd = T0.plusSeconds(3600);

        accruals.save(
                ProfitShareAccrual.pending(
                        "A-IN", "O-1", "ORG-L2", 100L, "CNY", 1, periodStart));
        accruals.save(
                ProfitShareAccrual.pending(
                        "A-IN2",
                        "O-2",
                        "ORG-L2",
                        200L,
                        "CNY",
                        1,
                        periodEnd.minusSeconds(1)));
        accruals.save(
                ProfitShareAccrual.pending(
                        "A-OUT-BEFORE",
                        "O-3",
                        "ORG-L2",
                        300L,
                        "CNY",
                        1,
                        periodStart.minusSeconds(1)));
        accruals.save(
                ProfitShareAccrual.pending(
                        "A-OUT-AT-END",
                        "O-4",
                        "ORG-L2",
                        400L,
                        "CNY",
                        1,
                        periodEnd));
        accruals.save(
                ProfitShareAccrual.rehydrate(
                        "A-SETTLED",
                        "O-5",
                        "ORG-L2",
                        500L,
                        "CNY",
                        1,
                        ProfitShareAccrual.Status.SETTLED,
                        periodStart.plusSeconds(10),
                        periodStart.plusSeconds(20),
                        "B-1",
                        null));

        var pending =
                accruals.findPendingCreatedBetween(periodStart, periodEnd).stream()
                        .map(ProfitShareAccrual::id)
                        .sorted()
                        .toList();

        assertEquals(2, pending.size());
        assertEquals("A-IN", pending.get(0));
        assertEquals("A-IN2", pending.get(1));
    }
}
