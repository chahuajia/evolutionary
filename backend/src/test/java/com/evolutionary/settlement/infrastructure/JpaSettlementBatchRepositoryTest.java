package com.evolutionary.settlement.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.settlement.application.SettlementBatchRepository;
import com.evolutionary.settlement.domain.SettlementBatch;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片44b：SettlementBatch JPA 落库。 */
@SpringBootTest
class JpaSettlementBatchRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T04:00:00Z");
    private static final Instant T7 = T0.plusSeconds(7L * 24 * 3600);

    @Autowired
    private SettlementBatchRepository batches;

    @Autowired
    private SettlementBatchJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("open → save → get 命中")
    void openSaveAndGet() {
        SettlementBatch open =
                SettlementBatch.open("BATCH-1", T0, T7, T0);
        batches.save(open);

        SettlementBatch found = batches.get("BATCH-1");
        assertEquals("BATCH-1", found.id());
        assertEquals(T0, found.periodStart());
        assertEquals(T7, found.periodEnd());
        assertEquals(SettlementBatch.Status.OPEN, found.status());
        assertEquals(T0, found.createdAt());
        assertNull(found.closedAt());
        assertTrue(jpa.findById("BATCH-1").isPresent());
    }

    @Test
    @DisplayName("close 后再 save → get 状态 CLOSED")
    void closeThenSaveAndGetClosed() {
        SettlementBatch closed =
                SettlementBatch.open("BATCH-2", T0, T7, T0).close(T7);
        batches.save(closed);

        SettlementBatch found = batches.get("BATCH-2");
        assertEquals(SettlementBatch.Status.CLOSED, found.status());
        assertEquals(T7, found.closedAt());
        assertEquals(SettlementBatch.Status.CLOSED, jpa.findById("BATCH-2").orElseThrow().getStatus());
    }
}
