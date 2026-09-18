package com.evolutionary.commerce.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.application.OrderRepository;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Order;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片37b：Order JPA 落库。 */
@SpringBootTest
class JpaOrderRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T08:00:00Z");
    private static final Instant T1 = Instant.parse("2026-09-18T08:01:00Z");

    @Autowired
    private OrderRepository orders;

    @Autowired
    private OrderJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("create → save → get 命中")
    void saveAndGetCreated() {
        orders.save(
                Order.create(
                        "O-JPA-1", "U-JPA", "P-JPA", "ORG-JPA", Money.cny(9_900), T0));

        Order found = orders.get("O-JPA-1");
        assertEquals("O-JPA-1", found.id());
        assertEquals("U-JPA", found.userId());
        assertEquals("P-JPA", found.productId());
        assertEquals("ORG-JPA", found.orgId());
        assertEquals(Order.Status.CREATED, found.status());
        assertEquals(9_900, found.paidAmount().cents());
        assertEquals(T0, found.createdAt());
        assertNull(found.paidAt());
        assertNull(found.refundedAt());
        assertTrue(jpa.findById("O-JPA-1").isPresent());
    }

    @Test
    @DisplayName("pay 后再 save 验证 paidAt/status")
    void payThenSavePersistsPaidAt() {
        Order created =
                Order.create(
                        "O-JPA-2", "U-JPA", "P-JPA", "ORG-JPA", Money.cny(3_000), T0);
        orders.save(created);

        Order paid = created.pay(T1);
        orders.save(paid);

        Order found = orders.get("O-JPA-2");
        assertEquals(Order.Status.PAID, found.status());
        assertEquals(T1, found.paidAt());
        assertNotNull(found.paidAt());

        OrderJpaEntity row = jpa.findById("O-JPA-2").orElseThrow();
        assertEquals(Order.Status.PAID, row.getStatus());
        assertEquals(T1, row.getPaidAt());
        assertEquals(3_000L, row.getPaidCents());
        assertEquals("CNY", row.getPaidCurrency());
    }
}
