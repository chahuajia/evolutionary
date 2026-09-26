package com.evolutionary.mall.infrastructure;


import com.evolutionary.commerce.domain.Order;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.PaymentIntent;
import com.evolutionary.mall.application.MallOrderRepository;
import com.evolutionary.mall.domain.MallOrder;
import com.evolutionary.mall.domain.MallOrderLine;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片42a：MallOrder JPA 落库 mall_orders。 */
@SpringBootTest
class JpaMallOrderRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T08:00:00Z");
    private static final Instant T1 = Instant.parse("2026-09-18T08:01:00Z");

    @Autowired
    private MallOrderRepository orders;

    @Autowired
    private MallOrderJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAll();
    }

    @Test
    @DisplayName("create → save → findById 命中")
    void saveAndFindCreated() {
        MallOrderLine line = new MallOrderLine("S1", 2, Money.cny(1_000));
        MallOrder created =
                MallOrder.create(
                        "MORD-JPA-1",
                        "U-JPA",
                        "M1",
                        List.of(line),
                        PaymentIntent.balanceOnly(Money.cny(2_000)),
                        null,
                        Money.cny(2_000),
                        T0);
        orders.save(created);

        MallOrder found = orders.findById("MORD-JPA-1").orElseThrow();
        assertEquals("MORD-JPA-1", found.id());
        assertEquals("U-JPA", found.userId());
        assertEquals("M1", found.merchantOrgId());
        assertEquals(MallOrder.Status.CREATED, found.status());
        assertEquals(1, found.lines().size());
        assertEquals("S1", found.lines().get(0).skuId());
        assertEquals(2, found.lines().get(0).qty());
        assertEquals(2_000, found.paidAmount().cents());
        assertEquals(T0, found.createdAt());
        assertNull(found.paidAt());
        assertTrue(jpa.findById("MORD-JPA-1").isPresent());
    }

    @Test
    @DisplayName("pay 后再 save 验证 paidAt/status")
    void payThenSavePersistsPaidAt() {
        MallOrderLine line = new MallOrderLine("S1", 1, Money.cny(3_000));
        MallOrder created =
                MallOrder.create(
                        "MORD-JPA-2",
                        "U-JPA",
                        "M1",
                        List.of(line),
                        PaymentIntent.balanceOnly(Money.cny(3_000)),
                        null,
                        Money.cny(3_000),
                        T0);
        orders.save(created);

        MallOrder paid = created.pay(T1);
        orders.save(paid);

        MallOrder found = orders.findById("MORD-JPA-2").orElseThrow();
        assertEquals(MallOrder.Status.PAID, found.status());
        assertEquals(T1, found.paidAt());
        assertNotNull(found.paidAt());

        MallOrderJpaEntity row = jpa.findById("MORD-JPA-2").orElseThrow();
        assertEquals(MallOrder.Status.PAID, row.getStatus());
        assertEquals(T1, row.getPaidAt());
        assertEquals(3_000L, row.getPaidCents());
        assertEquals("CNY", row.getPaidCurrency());
    }
}
