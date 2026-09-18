package com.evolutionary.commerce.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");
    private static final Instant T1 = Instant.parse("2026-09-17T01:00:00Z");

    private static Order created() {
        return Order.create("O-1", "U-1", "P-1", "ORG-1", Money.cny(9_900), T0);
    }

    @Test
    @DisplayName("新订单是 CREATED")
    void newOrderIsCreated() {
        assertEquals(Order.Status.CREATED, created().status());
    }

    @Test
    @DisplayName("CREATED 可支付为 PAID")
    void payFromCreated() {
        Order paid = created().pay(T1);
        assertEquals(Order.Status.PAID, paid.status());
        assertEquals(T1, paid.paidAt());
    }

    @Test
    @DisplayName("CREATED 可取消")
    void cancelFromCreated() {
        assertEquals(Order.Status.CANCELLED, created().cancel().status());
    }

    @Test
    @DisplayName("PAID 可退款")
    void refundFromPaid() {
        Order refunded = created().pay(T1).refund(T1.plusSeconds(60));
        assertEquals(Order.Status.REFUNDED, refunded.status());
    }

    @Test
    @DisplayName("CREATED 不能直接退款")
    void cannotRefundCreated() {
        assertThrows(Order.IllegalTransitionException.class, () -> created().refund(T1));
    }
}
