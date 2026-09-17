package com.evolutionary.mall.domain;

import com.evolutionary.commerce.domain.Money;
import java.util.Objects;

/** 商城订单行。 */
public record MallOrderLine(String skuId, int qty, Money unitPrice) {

    public MallOrderLine {
        if (skuId == null || skuId.isBlank()) {
            throw new IllegalArgumentException("skuId 不能为空");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("qty 必须为正");
        }
        Objects.requireNonNull(unitPrice, "unitPrice");
    }

    /** 行小计 = 单价 × 数量。 */
    public Money lineTotal() {
        return new Money(unitPrice.cents() * qty, unitPrice.currency());
    }
}
