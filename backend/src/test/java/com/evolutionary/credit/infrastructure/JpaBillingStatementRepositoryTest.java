package com.evolutionary.credit.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.BillingStatementRepository;
import com.evolutionary.credit.domain.BillingStatement;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片39a：BillingStatement JPA 落库。 */
@SpringBootTest
class JpaBillingStatementRepositoryTest {

    @Autowired
    private BillingStatementRepository statements;

    @Autowired
    private BillingStatementJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("issueDue → save → get 命中")
    void issueSaveAndGet() {
        BillingStatement issued =
                BillingStatement.issueDue(
                        "STMT-T1",
                        "U-T",
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-01-31T00:00:00Z"),
                        Money.cny(3_000),
                        Instant.parse("2026-02-01T00:00:00Z"));
        statements.save(issued);

        BillingStatement found = statements.get("STMT-T1");
        assertEquals("STMT-T1", found.id());
        assertEquals("U-T", found.userId());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), found.periodStart());
        assertEquals(Instant.parse("2026-01-31T00:00:00Z"), found.periodEnd());
        assertEquals(3_000, found.totalDue().cents());
        assertEquals(Currency.CNY, found.totalDue().currency());
        assertEquals(BillingStatement.Status.DUE, found.status());
        assertEquals(Instant.parse("2026-02-07T00:00:00Z"), found.dueDate());
        assertEquals(Instant.parse("2026-02-01T00:00:00Z"), found.createdAt());
        assertNull(found.paidAt());
        assertTrue(jpa.findById("STMT-T1").isPresent());
    }

    @Test
    @DisplayName("markPaid 后再 save 落库 status/paidAt")
    void markPaidThenSavePersistsStatusAndPaidAt() {
        statements.save(
                BillingStatement.issueDue(
                        "STMT-T2",
                        "U-T",
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-01-31T00:00:00Z"),
                        Money.cny(2_500),
                        Instant.parse("2026-02-01T00:00:00Z")));

        BillingStatement paid =
                statements.get("STMT-T2").markPaid(Instant.parse("2026-02-05T08:30:00Z"));
        statements.save(paid);

        BillingStatement found = statements.get("STMT-T2");
        assertEquals(BillingStatement.Status.PAID, found.status());
        assertEquals(Instant.parse("2026-02-05T08:30:00Z"), found.paidAt());

        BillingStatementJpaEntity row = jpa.findById("STMT-T2").orElseThrow();
        assertEquals(BillingStatement.Status.PAID, row.getStatus());
        assertEquals(Instant.parse("2026-02-05T08:30:00Z"), row.getPaidAt());
    }

    @Test
    @DisplayName("findByUserId 按 createdAt 倒序")
    void findByUserIdOrdersByCreatedAtDesc() {
        statements.save(
                BillingStatement.issueDue(
                        "STMT-OLD",
                        "U-S",
                        Instant.parse("2025-11-01T00:00:00Z"),
                        Instant.parse("2025-11-30T00:00:00Z"),
                        Money.cny(1_000),
                        Instant.parse("2025-12-01T00:00:00Z")));
        statements.save(
                BillingStatement.issueDue(
                        "STMT-NEW",
                        "U-S",
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-01-31T00:00:00Z"),
                        Money.cny(2_000),
                        Instant.parse("2026-02-01T00:00:00Z")));
        statements.save(
                BillingStatement.issueDue(
                        "STMT-OTHER",
                        "U-OTHER",
                        Instant.parse("2026-03-01T00:00:00Z"),
                        Instant.parse("2026-03-31T00:00:00Z"),
                        Money.cny(9_999),
                        Instant.parse("2026-04-01T00:00:00Z")));

        List<BillingStatement> found = statements.findByUserId("U-S");
        assertEquals(2, found.size());
        assertEquals("STMT-NEW", found.get(0).id());
        assertEquals("STMT-OLD", found.get(1).id());
    }
}
