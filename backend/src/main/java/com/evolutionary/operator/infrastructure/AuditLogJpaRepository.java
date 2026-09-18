package com.evolutionary.operator.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, String> {

    List<AuditLogJpaEntity> findByResourceIdOrderByCreatedAtAsc(String resourceId);
}
