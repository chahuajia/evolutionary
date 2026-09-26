package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.application.MerchantProfileRepository;
import com.evolutionary.mall.domain.MerchantProfile;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 商家档案 JPA 适配。 */
@Component
public final class JpaMerchantProfileRepository implements MerchantProfileRepository {

    private final MerchantProfileJpaRepository jpa;

    public JpaMerchantProfileRepository(MerchantProfileJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(MerchantProfile profile) {
        jpa.save(
                new MerchantProfileJpaEntity(
                        profile.orgId(), profile.shopName(), profile.status()));
    }

    @Override
    public Optional<MerchantProfile> findByOrgId(String orgId) {
        return jpa.findByOrgId(orgId).map(JpaMerchantProfileRepository::toDomain);
    }

    @Override
    public MerchantProfile getByOrgId(String orgId) {
        return findByOrgId(orgId)
                .orElseThrow(() -> new IllegalArgumentException("未知商家档案: " + orgId));
    }

    private static MerchantProfile toDomain(MerchantProfileJpaEntity row) {
        return MerchantProfile.rehydrate(row.getOrgId(), row.getShopName(), row.getStatus());
    }
}
