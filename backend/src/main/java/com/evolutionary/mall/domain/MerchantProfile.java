package com.evolutionary.mall.domain;

import java.util.Objects;

/** 商家档案（AC-40）。 */
public final class MerchantProfile {

    /**
     * 商家档案状态。
     */
    public enum Status {
        ACTIVE,
        SUSPENDED
    }


    private final String orgId;
    private final String shopName;
    private final MerchantProfile.Status status;

    private MerchantProfile(String orgId, String shopName, MerchantProfile.Status status) {
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
        return new MerchantProfile(orgId, shopName, MerchantProfile.Status.ACTIVE);
    }

    /** JPA 回放；不做业务校验。 */
    public static MerchantProfile rehydrate(
            String orgId, String shopName, MerchantProfile.Status status) {
        return new MerchantProfile(
                Objects.requireNonNull(orgId, "orgId"),
                Objects.requireNonNull(shopName, "shopName"),
                Objects.requireNonNull(status, "status"));
    }

    public boolean isActive() {
        return status == MerchantProfile.Status.ACTIVE;
    }

    public String orgId() {
        return orgId;
    }

    public String shopName() {
        return shopName;
    }

    public MerchantProfile.Status status() {
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
