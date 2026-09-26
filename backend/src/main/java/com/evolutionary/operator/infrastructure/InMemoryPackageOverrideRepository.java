package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.application.PackageOverrideRepository;
import com.evolutionary.operator.domain.PackageOverride;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内套餐覆盖仓储。 */
public final class InMemoryPackageOverrideRepository implements PackageOverrideRepository {

    private final Map<String, PackageOverride> byId = new ConcurrentHashMap<>();

    @Override
    public void save(PackageOverride override) {
        byId.put(override.id(), override);
    }

    @Override
    public Optional<PackageOverride> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<PackageOverride> findActiveByOrgAndTemplate(String orgId, String templateId) {
        return byId.values().stream()
                .filter(o -> o.orgId().equals(orgId))
                .filter(o -> o.templateId().equals(templateId))
                .filter(PackageOverride::isActive)
                .findFirst();
    }
}
