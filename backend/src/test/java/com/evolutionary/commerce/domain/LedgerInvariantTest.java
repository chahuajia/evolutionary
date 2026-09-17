package com.evolutionary.commerce.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LedgerInvariantTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");

    @Test
    @DisplayName("成对分录借贷平衡")
    void balancedEntriesPass() {
        Money amount = Money.cny(5_000);
        LedgerEntry payment =
                LedgerEntry.orderPayment("L-1", "ACC-U", "ACC-O", amount, "O-1", T0);

        assertDoesNotThrow(() -> LedgerInvariant.assertBalanced(List.of(payment)));
    }

    @Test
    @DisplayName("多笔支付分录仍借贷平衡")
    void multiplePaymentsStayBalanced() {
        LedgerEntry a =
                LedgerEntry.orderPayment("L-1", "ACC-U", "ACC-O", Money.cny(100), "O-1", T0);
        LedgerEntry b =
                LedgerEntry.orderPayment("L-2", "ACC-U2", "ACC-O", Money.cny(50), "O-2", T0);

        assertDoesNotThrow(() -> LedgerInvariant.assertBalanced(List.of(a, b)));
    }
}
