package com.evolutionary.commerce.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.application.ProductRepository;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.commerce.domain.ProductStatus;
import com.evolutionary.commerce.domain.SwapLimit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片36a：Product JPA 落库。 */
@SpringBootTest
class JpaProductRepositoryTest {

    @Autowired
    private ProductRepository products;

    @Autowired
    private ProductJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("save FIXED → get 命中")
    void saveAndGetFixed() {
        products.save(
                Product.create(
                        "P-JPA-1",
                        "ORG-JPA",
                        "月卡",
                        Money.cny(3_000),
                        30,
                        SwapLimit.finite(10),
                        ProductStatus.PUBLISHED));

        Product found = products.get("P-JPA-1");
        assertEquals("P-JPA-1", found.id());
        assertEquals("ORG-JPA", found.orgId());
        assertEquals(3_000, found.price().cents());
        assertEquals(30, found.durationDays());
        assertEquals(10, found.swapLimit().finiteOrNull());
        assertEquals(ProductStatus.PUBLISHED, found.status());
        assertTrue(jpa.findById("P-JPA-1").isPresent());
    }

    @Test
    @DisplayName("METERED 单价落库并可回放")
    void meteredPersistsRate() {
        products.save(
                Product.createMetered(
                        "P-JPA-M",
                        "ORG-JPA",
                        "计量按电量",
                        Money.cny(50),
                        ProductStatus.PUBLISHED));

        Product found = products.get("P-JPA-M");
        assertTrue(found.isMetered());
        assertEquals(50, found.meteredRate().cents());
        assertEquals(0, found.price().cents());

        ProductJpaEntity row = jpa.findById("P-JPA-M").orElseThrow();
        assertEquals(50L, row.getMeteredRateCents());
        assertEquals("CNY", row.getMeteredRateCurrency());
    }
}
