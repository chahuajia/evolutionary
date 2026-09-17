package com.evolutionary.operator.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 组织授权：按祖先链 + 资源归属判定，**禁止**用 level 数字。
 *
 * <p>对齐规格 P3-1 canManage。
 */
public final class OrgAuthorization {

    private final Map<String, Organization> byId;

    public OrgAuthorization(Map<String, Organization> organizations) {
        this.byId = Map.copyOf(Objects.requireNonNull(organizations, "organizations"));
    }

    /**
     * @param actorOrgId 操作者所属组织
     * @param resourceOwnerOrgId 资源归属组织
     * @param resourceRegion 资源区域（可 null 表示不做区域校验）
     */
    public boolean canManage(String actorOrgId, String resourceOwnerOrgId, String resourceRegion) {
        Organization actor = require(actorOrgId);
        Organization owner = require(resourceOwnerOrgId);
        if (!actor.isActive() || !owner.isActive()) {
            return false;
        }
        if (!isSelfOrAncestor(actorOrgId, resourceOwnerOrgId)) {
            return false;
        }
        if (resourceRegion != null && !actor.coversRegion(resourceRegion)) {
            return false;
        }
        return true;
    }

    /** actorOrg 是否等于 resourceOwner，或是其祖先。 */
    public boolean isSelfOrAncestor(String actorOrgId, String resourceOwnerOrgId) {
        if (actorOrgId.equals(resourceOwnerOrgId)) {
            return true;
        }
        Organization current = byId.get(resourceOwnerOrgId);
        while (current != null && current.parentId() != null) {
            if (actorOrgId.equals(current.parentId())) {
                return true;
            }
            current = byId.get(current.parentId());
        }
        return false;
    }

    /**
     * descendantOrg 是否为 ancestorOrg 的严格后代（不含自身）。
     *
     * <p>用于 PackageOverride：orgId 必须是 template.ownerOrg 的后代。
     */
    public boolean isDescendant(String descendantOrgId, String ancestorOrgId) {
        if (descendantOrgId.equals(ancestorOrgId)) {
            return false;
        }
        return isSelfOrAncestor(ancestorOrgId, descendantOrgId);
    }

    private Organization require(String orgId) {
        Organization org = byId.get(orgId);
        if (org == null) {
            throw new IllegalArgumentException("未知组织: " + orgId);
        }
        return org;
    }

    public static Map<String, Organization> index(Organization... orgs) {
        Map<String, Organization> map = new HashMap<>();
        for (Organization org : orgs) {
            map.put(org.id(), org);
        }
        return map;
    }

    public Optional<Organization> find(String orgId) {
        return Optional.ofNullable(byId.get(orgId));
    }
}
