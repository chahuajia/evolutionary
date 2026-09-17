package com.evolutionary.mall.domain;

import java.util.Objects;

/** 商家档案（AC-40）。 */
public final class MerchantProfile {

    private final String orgId;
    private final String shopName;
    private final MerchantStatus status;

    private MerchantProfile(String orgId, String shopName, MerchantStatus status) {
        this.orgId = orgId;
        this.shopName = shopName;
        this.status = status;
    }

    public static MerchantProfile activate(String orgId, String shopName) {
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId 不能为空");
        }
        if (shopName == null || shopName.isBlank()) {
            throw new IllegalArgumentException("shopName 不能为空");
        }
        return new MerchantProfile(orgId, shopName, MerchantStatus.ACTIVE);
    }

    public boolean isActive() {
        return status == MerchantStatus.ACTIVE;
    }

    public String orgId() {
        return orgId;
    }

    public String shopName() {
        return shopName;
    }

    public MerchantStatus status() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MerchantProfile m && Objects.equals(orgId, m.orgId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(orgId);
    }
}
