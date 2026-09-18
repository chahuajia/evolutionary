package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.application.MerchantProfileRepository;
import com.evolutionary.mall.domain.MerchantProfile;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内商家档案仓储。 */
public final class InMemoryMerchantProfileRepository implements MerchantProfileRepository {

    private final Map<String, MerchantProfile> byOrgId = new ConcurrentHashMap<>();

    @Override
    public void save(MerchantProfile profile) {
        byOrgId.put(profile.orgId(), profile);
    }

    @Override
    public Optional<MerchantProfile> findByOrgId(String orgId) {
        return Optional.ofNullable(byOrgId.get(orgId));
    }
}
