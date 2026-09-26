package com.evolutionary.commerce.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EntitlementTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");

    @Test
    @DisplayName("已支付订单可生成 ACTIVE 权益")
    void activeFromPaidOrder() {
        Product product =
                Product.create(
                        "P-1",
                        "ORG-1",
                        "30天卡",
                        Money.cny(9_900),
                        30,
                        Product.Status.PUBLISHED);
        Order paid = Order.create("O-1", "U-1", "P-1", "ORG-1", Money.cny(9_900), T0).pay(T0);

        Entitlement entitlement = Entitlement.createActive("E-1", paid, product, T0);

        assertTrue(entitlement.isActiveAt(T0.plusSeconds(1)));
    }

    @Test
    @DisplayName("未支付订单不能生成权益")
    void rejectsUnpaidOrder() {
        Product product =
                Product.create(
                        "P-1",
                        "ORG-1",
                        "30天卡",
                        Money.cny(9_900),
                        30,
                        Product.Status.PUBLISHED);
        Order created = Order.create("O-1", "U-1", "P-1", "ORG-1", Money.cny(9_900), T0);

        assertThrows(
                IllegalArgumentException.class,
                () -> Entitlement.createActive("E-1", created, product, T0));
    }

    @Test
    @DisplayName("revoke 后状态 REVOKED 且不再 active（INV-5）")
    void revokeMarksRevoked() {
        Product product =
                Product.create(
                        "P-1",
                        "ORG-1",
                        "30天卡",
                        Money.cny(9_900),
                        30,
                        Product.Status.PUBLISHED);
        Order paid = Order.create("O-1", "U-1", "P-1", "ORG-1", Money.cny(9_900), T0).pay(T0);
        Entitlement active = Entitlement.createActive("E-1", paid, product, T0);

        Entitlement revoked = active.revoke();

        assertEquals(Entitlement.Status.REVOKED, revoked.status());
        assertFalse(revoked.isActiveAt(T0.plusSeconds(1)));
    }
}
