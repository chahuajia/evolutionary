package com.evolutionary.mall.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.application.MallSkuRepository;
import com.evolutionary.mall.domain.MallOutcome;
import com.evolutionary.mall.domain.MallSku;
import com.evolutionary.mall.domain.MallSkuStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片40a：MallSku JPA 落库。 */
@SpringBootTest
class JpaMallSkuRepositoryTest {

    @Autowired
    private MallSkuRepository skus;

    @Autowired
    private MallSkuJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("createOnSale → save → get 命中")
    void saveAndGetOnSale() {
        skus.save(MallSku.createOnSale("S-JPA-1", "M1", "商城配件", Money.cny(1_000), 20));

        MallSku found = skus.get("S-JPA-1");
        assertEquals("S-JPA-1", found.id());
        assertEquals("M1", found.merchantOrgId());
        assertEquals("商城配件", found.name());
        assertEquals(1_000, found.price().cents());
        assertEquals(20, found.stock());
        assertEquals(MallSkuStatus.ON_SALE, found.status());
        assertTrue(jpa.findById("S-JPA-1").isPresent());
    }

    @Test
    @DisplayName("deductStock 后再 save 验证 stock")
    void deductStockThenSave() {
        MallSku sku = MallSku.createOnSale("S-JPA-2", "M1", "配件", Money.cny(500), 10);
        skus.save(sku);

        MallOutcome<MallSku> outcome = sku.deductStock(3);
        assertInstanceOf(MallOutcome.Ok.class, outcome);
        MallSku updated = ((MallOutcome.Ok<MallSku>) outcome).value();
        skus.save(updated);

        assertEquals(7, skus.get("S-JPA-2").stock());
        assertEquals(7, jpa.findById("S-JPA-2").orElseThrow().getStock());
    }
}
