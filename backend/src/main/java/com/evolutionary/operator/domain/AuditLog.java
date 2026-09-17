package com.evolutionary.operator.domain;

import java.time.Instant;
import java.util.Objects;

/** 运营审计日志（P3-6；本切片仅记 template.publish）。 */
public final class AuditLog {

    private final String id;
    private final String actorUserId;
    private final String orgId;
    private final AuditAction action;
    private final String resourceType;
    private final String resourceId;
    private final Instant createdAt;

    private AuditLog(
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

    public static AuditLog of(
            String id,
            String actorUserId,
            String orgId,
            AuditAction action,
            String resourceType,
            String resourceId,
            Instant createdAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("audit id 不能为空");
        }
        if (actorUserId == null || actorUserId.isBlank()) {
            throw new IllegalArgumentException("actorUserId 不能为空");
        }
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId 不能为空");
        }
        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("resourceType 不能为空");
        }
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("resourceId 不能为空");
        }
        return new AuditLog(
                id,
                actorUserId,
                orgId,
                Objects.requireNonNull(action, "action"),
                resourceType,
                resourceId,
                Objects.requireNonNull(createdAt, "createdAt"));
    }

    public String id() {
        return id;
    }

    public String actorUserId() {
        return actorUserId;
    }

    public String orgId() {
        return orgId;
    }

    public AuditAction action() {
        return action;
    }

    public String resourceType() {
        return resourceType;
    }

    public String resourceId() {
        return resourceId;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
