package com.evolutionary.operator.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data 仓储（表 organizations）。 */
public interface OrganizationJpaRepository
        extends JpaRepository<OrganizationJpaEntity, String> {}
