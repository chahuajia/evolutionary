package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.domain.AuditAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 运营审计持久化 —— 只活在 infrastructure。 */
@Entity
@Table(name = "audit_logs")
public class AuditLogJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String actorUserId;

    @Column(nullable = false)
    private String orgId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(nullable = false)
    private String resourceType;

    @Column(nullable = false)
    private String resourceId;

    @Column(nullable = false)
    private Instant createdAt;

    protected AuditLogJpaEntity() {}

    public AuditLogJpaEntity(
            String id,
            String actorUserId,
            String orgId,
            AuditAction action,
            String resourceType,
            String resourceId,
            Instant createdAt) {
        this.id = id;
        this.actorUserId = actorUserId;
        this.orgId = orgId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getActorUserId() {
        return actorUserId;
    }

    public String getOrgId() {
        return orgId;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
