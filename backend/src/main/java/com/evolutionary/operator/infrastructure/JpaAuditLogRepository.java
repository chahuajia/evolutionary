package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.application.AuditLogRepository;
import com.evolutionary.operator.domain.AuditLog;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 运营审计 JPA 适配（append-only）。 */
@Component
public final class JpaAuditLogRepository implements AuditLogRepository {

    private final AuditLogJpaRepository jpa;

    public JpaAuditLogRepository(AuditLogJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void append(AuditLog log) {
        jpa.save(
                new AuditLogJpaEntity(
                        log.id(),
                        log.actorUserId(),
                        log.orgId(),
                        log.action(),
                        log.resourceType(),
                        log.resourceId(),
                        log.createdAt()));
    }

    @Override
    public List<AuditLog> findByResourceId(String resourceId) {
        List<AuditLog> result = new ArrayList<>();
        for (AuditLogJpaEntity row : jpa.findByResourceIdOrderByCreatedAtAsc(resourceId)) {
            result.add(
                    AuditLog.of(
                            row.getId(),
                            row.getActorUserId(),
                            row.getOrgId(),
                            row.getAction(),
                            row.getResourceType(),
                            row.getResourceId(),
                            row.getCreatedAt()));
        }
        return result;
    }
}
