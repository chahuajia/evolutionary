package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.Organization;
import java.util.List;
import java.util.Optional;

/** 组织仓储端口。 */
public interface OrganizationRepository {

    void save(Organization org);

    Optional<Organization> findById(String id);

    /** 全部组织（用于 OrgAuthorization.index）。 */
    List<Organization> findAll();

    default Organization get(String id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("未知组织: " + id));
    }
}
