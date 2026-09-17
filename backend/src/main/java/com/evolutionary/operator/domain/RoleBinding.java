package com.evolutionary.operator.domain;

import java.util.Objects;

/** 用户在某组织下的角色绑定。 */
public final class RoleBinding {

    private final String userId;
    private final String orgId;
    private final OperatorRole role;

    private RoleBinding(String userId, String orgId, OperatorRole role) {
        this.userId = userId;
        this.orgId = orgId;
        this.role = role;
    }

    public static RoleBinding bind(String userId, String orgId, OperatorRole role) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId 不能为空");
        }
        return new RoleBinding(userId, orgId, Objects.requireNonNull(role, "role"));
    }

    public String userId() {
        return userId;
    }

    public String orgId() {
        return orgId;
    }

    public OperatorRole role() {
        return role;
    }
}
