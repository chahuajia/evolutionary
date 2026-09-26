package com.evolutionary.credit.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditPolicyJpaRepository extends JpaRepository<CreditPolicyJpaEntity, String> {

    Optional<CreditPolicyJpaEntity> findByVersion(int version);
}
