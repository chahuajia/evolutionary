package com.evolutionary.mall.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.evolutionary.commerce.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MallSkuTest {

    @Test
    @DisplayName("上架 SKU 可扣库存")
    void deductStockOk() {
        MallSku sku = MallSku.createOnSale("S1", "M1", "配件", Money.cny(5_000), 3);
        MallOutcome<MallSku> outcome = sku.deductStock(2);

        assertInstanceOf(MallOutcome.Ok.class, outcome);
        assertEquals(1, ((MallOutcome.Ok<MallSku>) outcome).value().stock());
    }

    @Test
    @DisplayName("库存不足返回 INSUFFICIENT_STOCK")
    void insufficientStock() {
        MallSku sku = MallSku.createOnSale("S1", "M1", "配件", Money.cny(5_000), 1);
        MallOutcome<MallSku> outcome = sku.deductStock(2);

        assertInstanceOf(MallOutcome.Err.class, outcome);
        assertEquals(
                MallErrorCode.INSUFFICIENT_STOCK,
                ((MallOutcome.Err<MallSku>) outcome).code());
    }
}
