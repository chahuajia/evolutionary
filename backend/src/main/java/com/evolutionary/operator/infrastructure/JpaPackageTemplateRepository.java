package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.application.PackageTemplateRepository;
import com.evolutionary.operator.domain.PackageTemplate;
import com.evolutionary.operator.domain.TemplateBaseProduct;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 套餐模板 JPA 适配（表 package_templates）。 */
@Component
public final class JpaPackageTemplateRepository implements PackageTemplateRepository {

    private final PackageTemplateJpaRepository jpa;

    public JpaPackageTemplateRepository(PackageTemplateJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(PackageTemplate template) {
        TemplateBaseProduct base = template.baseProduct();
        jpa.save(
                new PackageTemplateJpaEntity(
                        template.id(),
                        template.ownerOrgId(),
                        template.version(),
                        base.displayName(),
                        base.priceCents(),
                        base.durationDays(),
                        template.status(),
                        template.inheritedFrom(),
                        template.allowedOverrideFields(),
                        template.publishedAt()));
    }

    @Override
    public Optional<PackageTemplate> findById(String id) {
        return jpa.findById(id).map(JpaPackageTemplateRepository::toDomain);
    }

    private static PackageTemplate toDomain(PackageTemplateJpaEntity row) {
        return PackageTemplate.rehydrate(
                row.getId(),
                row.getOwnerOrgId(),
                row.getVersion(),
                TemplateBaseProduct.of(
                        row.getBaseDisplayName(), row.getBasePriceCents(), row.getBaseDurationDays()),
                row.getStatus(),
                row.getInheritedFrom(),
                row.getAllowedOverrideFields(),
                row.getPublishedAt());
    }
}
