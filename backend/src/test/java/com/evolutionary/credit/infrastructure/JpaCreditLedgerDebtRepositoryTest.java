package com.evolutionary.credit.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.CreditLedgerDebtRepository;
import com.evolutionary.credit.domain.CreditLedgerDebt;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片39b：CreditLedgerDebt JPA 落库。 */
@SpringBootTest
class JpaCreditLedgerDebtRepositoryTest {

    @Autowired
    private CreditLedgerDebtRepository debts;

    @Autowired
    private CreditLedgerDebtJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("open → save → get 命中")
    void openSaveAndGet() {
        Instant createdAt = Instant.parse("2026-03-01T00:00:00Z");
        debts.save(CreditLedgerDebt.open("D-1", "U-T", "O-1", Money.cny(3_000), createdAt));

        CreditLedgerDebt found = debts.get("D-1");
        assertEquals("D-1", found.id());
        assertEquals("U-T", found.userId());
        assertEquals("O-1", found.orderId());
        assertEquals(3_000, found.amount().cents());
        assertEquals(CreditLedgerDebt.Status.OPEN, found.status());
        assertEquals(createdAt, found.createdAt());
        assertNull(found.billedStatementId());
        assertNull(found.paidAt());
        assertTrue(jpa.findById("D-1").isPresent());
    }

    @Test
    @DisplayName("markBilled 后按 billedStatementId 查")
    void markBilledThenFindByBilledStatementId() {
        CreditLedgerDebt opened =
                CreditLedgerDebt.open(
                        "D-2", "U-T", "O-2", Money.cny(1_500),
                        Instant.parse("2026-03-01T00:00:00Z"));
        debts.save(opened);
        debts.save(opened.markBilled("STMT-T"));

        List<CreditLedgerDebt> billed = debts.findByBilledStatementId("STMT-T");
        assertEquals(1, billed.size());
        assertEquals("D-2", billed.get(0).id());
        assertEquals(CreditLedgerDebt.Status.BILLED, billed.get(0).status());
        assertEquals("STMT-T", billed.get(0).billedStatementId());
    }

    @Test
    @DisplayName("findByUserIdAndStatus 按用户与状态过滤")
    void findByUserIdAndStatusFilters() {
        Instant at = Instant.parse("2026-03-01T00:00:00Z");
        debts.save(CreditLedgerDebt.open("D-3", "U-T", "O-3", Money.cny(100), at));
        debts.save(CreditLedgerDebt.open("D-4", "U-T", "O-4", Money.cny(200), at));
        debts.save(CreditLedgerDebt.open("D-5", "U-X", "O-5", Money.cny(300), at));

        List<CreditLedgerDebt> openOfUT = debts.findByUserIdAndStatus("U-T", CreditLedgerDebt.Status.OPEN);
        assertEquals(2, openOfUT.size());
        assertTrue(openOfUT.stream().allMatch(d -> d.userId().equals("U-T")));

        List<CreditLedgerDebt> allOpen = debts.findByStatus(CreditLedgerDebt.Status.OPEN);
        assertEquals(3, allOpen.size());

        List<CreditLedgerDebt> byOrder = debts.findByOrderId("O-5");
        assertEquals(1, byOrder.size());
        assertEquals("D-5", byOrder.get(0).id());
    }
}
