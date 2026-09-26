package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.application.PackageOverrideRepository;
import com.evolutionary.operator.domain.OverridePatches;
import com.evolutionary.operator.domain.PackageOverride;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 套餐覆盖 JPA 适配（表 package_overrides）。 */
@Component
public final class JpaPackageOverrideRepository implements PackageOverrideRepository {

    private final PackageOverrideJpaRepository jpa;

    public JpaPackageOverrideRepository(PackageOverrideJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(PackageOverride override) {
        jpa.save(
                new PackageOverrideJpaEntity(
                        override.id(),
                        override.orgId(),
                        override.templateId(),
                        override.templateVersion(),
                        override.allowedFields(),
                        override.patches().priceCents().orElse(null),
                        override.patches().displayName().orElse(null),
                        override.effectiveFrom(),
                        override.effectiveUntil(),
                        override.status()));
    }

    @Override
    public Optional<PackageOverride> findById(String id) {
        return jpa.findById(id).map(JpaPackageOverrideRepository::toDomain);
    }

    @Override
    public Optional<PackageOverride> findActiveByOrgAndTemplate(
            String orgId, String templateId) {
        return jpa.findFirstByOrgIdAndTemplateIdAndStatus(
                        orgId, templateId, PackageOverride.Status.ACTIVE)
                .map(JpaPackageOverrideRepository::toDomain);
    }

    private static PackageOverride toDomain(PackageOverrideJpaEntity row) {
        return PackageOverride.rehydrate(
                row.getId(),
                row.getOrgId(),
                row.getTemplateId(),
                row.getTemplateVersion(),
                row.getAllowedFields(),
                OverridePatches.of(row.getPatchPriceCents(), row.getPatchDisplayName()),
                row.getEffectiveFrom(),
                row.getEffectiveUntil(),
                row.getStatus());
    }
}
