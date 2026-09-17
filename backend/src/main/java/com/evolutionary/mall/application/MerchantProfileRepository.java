package com.evolutionary.mall.application;

import com.evolutionary.mall.domain.MerchantProfile;
import java.util.Optional;

/** 商家档案仓储端口。 */
public interface MerchantProfileRepository {

    void save(MerchantProfile profile);

    Optional<MerchantProfile> findByOrgId(String orgId);

    default MerchantProfile getByOrgId(String orgId) {
        return findByOrgId(orgId)
                .orElseThrow(() -> new IllegalArgumentException("未知商家档案: " + orgId));
    }
}
