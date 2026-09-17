package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.PackageOverride;
import java.util.Optional;

/** 套餐覆盖仓储端口（无 Spring/JPA）。 */
public interface PackageOverrideRepository {

    void save(PackageOverride override);

    Optional<PackageOverride> findById(String id);

    /** 某组织对某模板当前激活的覆盖（至多一条）。 */
    Optional<PackageOverride> findActiveByOrgAndTemplate(String orgId, String templateId);

    default PackageOverride get(String id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("未知覆盖: " + id));
    }
}
