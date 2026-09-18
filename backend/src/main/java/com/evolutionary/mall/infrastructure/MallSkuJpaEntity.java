package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.domain.MallSkuStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mall_skus")
public class MallSkuJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String merchantOrgId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long priceCents;

    @Column(nullable = false)
    private String priceCurrency;

    @Column(nullable = false)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MallSkuStatus status;

    protected MallSkuJpaEntity() {}

    public MallSkuJpaEntity(
            String id,
            String merchantOrgId,
            String name,
            long priceCents,
            String priceCurrency,
            int stock,
            MallSkuStatus status) {
        this.id = id;
        this.merchantOrgId = merchantOrgId;
        this.name = name;
        this.priceCents = priceCents;
        this.priceCurrency = priceCurrency;
        this.stock = stock;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getMerchantOrgId() {
        return merchantOrgId;
    }

    public String getName() {
        return name;
    }

    public long getPriceCents() {
        return priceCents;
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }

    public int getStock() {
        return stock;
    }

    public MallSkuStatus getStatus() {
        return status;
    }
}
