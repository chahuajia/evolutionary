package com.evolutionary.settlement.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfitSharingRuleJpaRepository
        extends JpaRepository<ProfitSharingRuleJpaEntity, String> {

    Optional<ProfitSharingRuleJpaEntity> findByOrgId(String orgId);
}
