package com.evolutionary.mall.domain;


import com.evolutionary.commerce.domain.Order;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Money;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MallOrderTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");

    @Test
    @DisplayName("CREATED → PAID，paidAmount 保持下单金额")
    void payKeepsPaidAmount() {
        MallOrderLine line = new MallOrderLine("S1", 1, Money.cny(5_000));
        MallOrder created =
                MallOrder.create("mord-1", "U1", "M1", List.of(line), Money.cny(5_000), T0);

        MallOrder paid = created.pay(T0.plusSeconds(1));

        assertTrue(paid.isPaid());
        assertEquals(MallOrder.Status.PAID, paid.status());
        assertEquals(5_000, paid.paidAmount().cents());
        assertEquals(T0.plusSeconds(1), paid.paidAt());
    }

    @Test
    @DisplayName("非 CREATED 不可再 pay")
    void cannotPayTwice() {
        MallOrderLine line = new MallOrderLine("S1", 1, Money.cny(5_000));
        MallOrder paid =
                MallOrder.create("mord-1", "U1", "M1", List.of(line), Money.cny(5_000), T0)
                        .pay(T0);

        assertThrows(MallOrder.IllegalTransitionException.class, () -> paid.pay(T0));
    }
}
