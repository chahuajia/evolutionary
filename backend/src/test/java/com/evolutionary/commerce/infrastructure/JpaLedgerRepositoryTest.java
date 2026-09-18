package com.evolutionary.commerce.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerRefType;
import com.evolutionary.commerce.domain.Money;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片36b：LedgerEntry JPA 落库。 */
@SpringBootTest
class JpaLedgerRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T04:00:00Z");

    @Autowired
    private LedgerRepository ledger;

    @Autowired
    private LedgerEntryJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("append → findByOrderId / findAll 命中")
    void appendAndFindByOrderId() {
        ledger.append(
                LedgerEntry.orderPaymentBalance(
                        "L-1", "ACC-U", "ACC-O", Money.cny(1000), "O-1", T0));

        List<LedgerEntry> byOrder = ledger.findByOrderId("O-1");
        assertEquals(1, byOrder.size());
        assertEquals("L-1", byOrder.get(0).id());
        assertEquals(LedgerRefType.ORDER_PAYMENT_BALANCE, byOrder.get(0).refType());
        assertEquals(1000, byOrder.get(0).amount().cents());

        assertEquals(1, ledger.findAll().size());
        assertTrue(jpa.findById("L-1").isPresent());
    }

    @Test
    @DisplayName("METERED_CHARGE 落库并可按 refId 回放")
    void meteredChargePersists() {
        ledger.append(
                LedgerEntry.meteredCharge(
                        "L-M", "ACC-U", "ACC-O", Money.cny(1250), "UE-1", T0));

        List<LedgerEntry> found = ledger.findByRefId("UE-1");
        assertEquals(1, found.size());
        assertEquals(LedgerRefType.METERED_CHARGE, found.get(0).refType());
        assertEquals("UE-1", found.get(0).refId());
        assertEquals(1250, found.get(0).amount().cents());
    }
}
