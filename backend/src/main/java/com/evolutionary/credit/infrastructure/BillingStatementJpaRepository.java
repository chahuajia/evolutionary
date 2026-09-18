package com.evolutionary.credit.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingStatementJpaRepository extends JpaRepository<BillingStatementJpaEntity, String> {

    List<BillingStatementJpaEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
