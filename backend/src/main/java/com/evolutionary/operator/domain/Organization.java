package com.evolutionary.operator.domain;

import java.util.List;
import java.util.Objects;

/**
 * 统一组织模型。
 *
 * <p>{@code level} 若将来展示需要，不得作为授权依据（规格 P3-1）。
 */
public final class Organization {

    private final String id;
    private final String name;
    private final String parentId;
    private final List<String> regionScope;
    private final OrgStatus status;

    private Organization(
            String id, String name, String parentId, List<String> regionScope, OrgStatus status) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.regionScope = List.copyOf(regionScope);
        this.status = status;
    }

    public static Organization createRoot(String id, String name, List<String> regionScope) {
        return create(id, name, null, regionScope, OrgStatus.ACTIVE);
    }

    public static Organization createChild(
            String id, String name, String parentId, List<String> regionScope) {
        if (parentId == null || parentId.isBlank()) {
            throw new IllegalArgumentException("子组织必须有 parentId");
        }
        return create(id, name, parentId, regionScope, OrgStatus.ACTIVE);
    }

    public static Organization create(
            String id, String name, String parentId, List<String> regionScope, OrgStatus status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("org id 不能为空");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name 不能为空");
        }
        Objects.requireNonNull(regionScope, "regionScope");
        Objects.requireNonNull(status, "status");
        return new Organization(id, name, parentId, regionScope, status);
    }

    public boolean isActive() {
        return status == OrgStatus.ACTIVE;
    }

    public boolean coversRegion(String region) {
        return regionScope.contains(region);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String parentId() {
        return parentId;
    }

    public List<String> regionScope() {
        return regionScope;
    }

    public OrgStatus status() {
        return status;
    }
}
