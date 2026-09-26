package com.evolutionary.operator.domain;

import java.util.List;
import java.util.Objects;

/**
 * 统一组织模型。
 *
 * <p>{@code level} 若将来展示需要，不得作为授权依据（规格 P3-1）。
 */
public final class Organization {

    /**
     * 组织状态（对齐 IDL Organization.Status）。
     */
    public enum Status {
        PENDING,
        ACTIVE,
        SUSPENDED
    }


    private final String id;
    private final String name;
    private final String parentId;
    private final List<OrgCapability> capabilities;
    private final List<String> regionScope;
    private final Organization.Status status;

    private Organization(
            String id,
            String name,
            String parentId,
            List<OrgCapability> capabilities,
            List<String> regionScope,
            Organization.Status status) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.capabilities = List.copyOf(capabilities);
        this.regionScope = List.copyOf(regionScope);
        this.status = status;
    }

    public static Organization createRoot(String id, String name, List<String> regionScope) {
        return create(id, name, null, List.of(OrgCapability.OPERATOR), regionScope, Organization.Status.ACTIVE);
    }

    public static Organization createChild(
            String id, String name, String parentId, List<String> regionScope) {
        if (parentId == null || parentId.isBlank()) {
            throw new IllegalArgumentException("子组织必须有 parentId");
        }
        return create(
                id, name, parentId, List.of(OrgCapability.OPERATOR), regionScope, Organization.Status.ACTIVE);
    }

    public static Organization create(
            String id,
            String name,
            String parentId,
            List<OrgCapability> capabilities,
            List<String> regionScope,
            Organization.Status status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("org id 不能为空");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name 不能为空");
        }
        Objects.requireNonNull(capabilities, "capabilities");
        Objects.requireNonNull(regionScope, "regionScope");
        Objects.requireNonNull(status, "status");
        return new Organization(id, name, parentId, capabilities, regionScope, status);
    }

    /**
     * 从持久化回放。
     *
     * <p>与 {@link #create} 的区别只是**语义**：工厂表达"新建一份"，
     * rehydrate 表达"把已有的读回来"。两者校验同一组形状约束 ——
     * 一条从库里读出来就不合法的记录，说明库坏了，应该在被读到的那一刻就炸。
     */
    public static Organization rehydrate(
            String id,
            String name,
            String parentId,
            List<OrgCapability> capabilities,
            List<String> regionScope,
            Organization.Status status) {
        return create(id, name, parentId, capabilities, regionScope, status);
    }

    public boolean isActive() {
        return status == Organization.Status.ACTIVE;
    }

    public boolean hasCapability(OrgCapability capability) {
        return capabilities.contains(capability);
    }

    /** 授予能力（不可变拷贝）。 */
    public Organization grant(OrgCapability capability) {
        if (capabilities.contains(capability)) {
            return this;
        }
        java.util.ArrayList<OrgCapability> next = new java.util.ArrayList<>(capabilities);
        next.add(capability);
        return new Organization(id, name, parentId, next, regionScope, status);
    }

    /** 挂上级组织（仅当尚无 parentId）。 */
    public Organization withParent(String newParentId) {
        if (newParentId == null || newParentId.isBlank()) {
            throw new IllegalArgumentException("parentId 不能为空");
        }
        if (parentId != null) {
            return this;
        }
        return new Organization(id, name, newParentId, capabilities, regionScope, status);
    }

    /** 仅 MERCHANT、无 OPERATOR 时不可发套餐模板。 */
    public boolean canPublishPackageTemplate() {
        return hasCapability(OrgCapability.OPERATOR);
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

    public List<OrgCapability> capabilities() {
        return capabilities;
    }

    public List<String> regionScope() {
        return regionScope;
    }

    public Organization.Status status() {
        return status;
    }
}
