package com.evolutionary.mall.infrastructure;


import com.evolutionary.mall.domain.MallSku;
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
    private MallSku.Status status;

    protected MallSkuJpaEntity() {}

    public MallSkuJpaEntity(
            String id,
            String merchantOrgId,
            String name,
            long priceCents,
            String priceCurrency,
            int stock,
            MallSku.Status status) {
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

    public MallSku.Status getStatus() {
        return status;
    }
}
