package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.PackageTemplate;
import java.util.Optional;

/** 套餐模板仓储端口（无 Spring/JPA）。 */
public interface PackageTemplateRepository {

    void save(PackageTemplate template);

    Optional<PackageTemplate> findById(String id);

    default PackageTemplate get(String id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("未知模板: " + id));
    }
}
