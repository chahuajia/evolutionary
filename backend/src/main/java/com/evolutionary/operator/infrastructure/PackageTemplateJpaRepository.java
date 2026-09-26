package com.evolutionary.operator.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data 仓储（表 package_templates）。 */
public interface PackageTemplateJpaRepository
        extends JpaRepository<PackageTemplateJpaEntity, String> {}
