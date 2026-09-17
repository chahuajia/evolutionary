package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.Organization;
import java.util.Optional;

/** 组织仓储端口。 */
public interface OrganizationRepository {

    void save(Organization org);

    Optional<Organization> findById(String id);

    default Organization get(String id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("未知组织: " + id));
    }
}
