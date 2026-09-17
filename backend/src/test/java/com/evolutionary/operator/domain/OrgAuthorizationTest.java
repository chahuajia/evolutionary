package com.evolutionary.operator.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrgAuthorizationTest {

    private Organization l1;
    private Organization l2;
    private OrgAuthorization auth;

    @BeforeEach
    void setUp() {
        l1 = Organization.createRoot("ORG-L1", "华南", List.of("GD", "SZ"));
        l2 = Organization.createChild("ORG-L2", "深圳", "ORG-L1", List.of("SZ"));
        auth = new OrgAuthorization(OrgAuthorization.index(l1, l2));
    }

    @Test
    @DisplayName("本组织可管理本组织资源")
    void selfCanManage() {
        assertTrue(auth.canManage("ORG-L2", "ORG-L2", "SZ"));
    }

    @Test
    @DisplayName("祖先组织可管理子孙资源")
    void ancestorCanManage() {
        assertTrue(auth.canManage("ORG-L1", "ORG-L2", "SZ"));
    }

    @Test
    @DisplayName("子孙不能管理祖先资源")
    void childCannotManageParent() {
        assertFalse(auth.canManage("ORG-L2", "ORG-L1", "GD"));
    }

    @Test
    @DisplayName("区域不在 actor.regionScope 内则拒绝")
    void regionOutOfScope() {
        assertFalse(auth.canManage("ORG-L2", "ORG-L2", "GD"));
    }
}
