package com.evolutionary.mall.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MallOrderLineJpaEmbeddable {

    @Column(nullable = false)
    private String skuId;

    @Column(nullable = false)
    private int qty;

    @Column(nullable = false)
    private long unitPriceCents;

    @Column(nullable = false)
    private String unitPriceCurrency;

    protected MallOrderLineJpaEmbeddable() {}

    public MallOrderLineJpaEmbeddable(
            String skuId, int qty, long unitPriceCents, String unitPriceCurrency) {
        this.skuId = skuId;
        this.qty = qty;
        this.unitPriceCents = unitPriceCents;
        this.unitPriceCurrency = unitPriceCurrency;
    }

    public String getSkuId() {
        return skuId;
    }

    public int getQty() {
        return qty;
    }

    public long getUnitPriceCents() {
        return unitPriceCents;
    }

    public String getUnitPriceCurrency() {
        return unitPriceCurrency;
    }
}
