package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.domain.PackageOverride;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data 仓储（表 package_overrides）。 */
public interface PackageOverrideJpaRepository
        extends JpaRepository<PackageOverrideJpaEntity, String> {

    /** 某组织对某模板**当前激活**的覆盖（至多一条）—— 与 InMemory 版同语义。 */
    Optional<PackageOverrideJpaEntity> findFirstByOrgIdAndTemplateIdAndStatus(
            String orgId, String templateId, PackageOverride.Status status);
}
