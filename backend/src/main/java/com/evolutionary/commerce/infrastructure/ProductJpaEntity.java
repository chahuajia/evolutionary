package com.evolutionary.commerce.infrastructure;


import com.evolutionary.commerce.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String orgId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long priceCents;

    @Column(nullable = false)
    private String priceCurrency;

    @Column(nullable = false)
    private int durationDays;

    /** null = UNLIMITED */
    private Integer swapLimitFinite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Product.Status status;

    private Long meteredRateCents;

    private String meteredRateCurrency;

    protected ProductJpaEntity() {}

    public ProductJpaEntity(
            String id,
            String orgId,
            String name,
            long priceCents,
            String priceCurrency,
            int durationDays,
            Integer swapLimitFinite,
            Product.Status status,
            Long meteredRateCents,
            String meteredRateCurrency) {
        this.id = id;
        this.orgId = orgId;
        this.name = name;
        this.priceCents = priceCents;
        this.priceCurrency = priceCurrency;
        this.durationDays = durationDays;
        this.swapLimitFinite = swapLimitFinite;
        this.status = status;
        this.meteredRateCents = meteredRateCents;
        this.meteredRateCurrency = meteredRateCurrency;
    }

    public String getId() {
        return id;
    }

    public String getOrgId() {
        return orgId;
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

    public int getDurationDays() {
        return durationDays;
    }

    public Integer getSwapLimitFinite() {
        return swapLimitFinite;
    }

    public Product.Status getStatus() {
        return status;
    }

    public Long getMeteredRateCents() {
        return meteredRateCents;
    }

    public String getMeteredRateCurrency() {
        return meteredRateCurrency;
    }
}
