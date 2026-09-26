package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.domain.MerchantProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "merchant_profiles")
public class MerchantProfileJpaEntity {

    @Id
    private String orgId;

    @Column(nullable = false)
    private String shopName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantProfile.Status status;

    protected MerchantProfileJpaEntity() {}

    public MerchantProfileJpaEntity(
            String orgId, String shopName, MerchantProfile.Status status) {
        this.orgId = orgId;
        this.shopName = shopName;
        this.status = status;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getShopName() {
        return shopName;
    }

    public MerchantProfile.Status getStatus() {
        return status;
    }
}
