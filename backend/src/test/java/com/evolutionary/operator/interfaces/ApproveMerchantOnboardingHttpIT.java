package com.evolutionary.operator.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.evolutionary.operator.application.AuditLogRepository;
import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 切片26a：平台总后台批准商家入驻 HTTP（AC-40 · APP-M1 / ORG-NEW · /admin）。
 * 切片30b：成功路径写 ONBOARDING_APPROVE；失败无审计。
 *
 * <p>{@link DirtiesContext}：批准写 APPROVED + MerchantProfile，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApproveMerchantOnboardingHttpIT {

    private static final String SHOP_BODY = "{\"shopName\":\"黑鸟旗舰店\"}";

    @Autowired private MockMvc mvc;
    @Autowired private AuditLogRepository auditLogs;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("POST /admin/onboarding/APP-M1/approve → 200 ORG-NEW ACTIVE + ONBOARDING_APPROVE")
    void approveAppM1Ok() throws Exception {
        mvc.perform(
                        post("/admin/onboarding/APP-M1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(SHOP_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantOrgId").value("ORG-NEW"))
                .andExpect(jsonPath("$.shopName").value("黑鸟旗舰店"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        List<AuditLog> logs = auditLogs.findByResourceId("APP-M1");
        assertEquals(1, logs.size());
        assertEquals(AuditAction.ONBOARDING_APPROVE, logs.get(0).action());
        assertEquals("OnboardingApplication", logs.get(0).resourceType());
        assertEquals("U-PLATFORM", logs.get(0).actorUserId());
        assertEquals("PLATFORM", logs.get(0).orgId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("重复批准 APP-M1 → 403 CAPABILITY_DENIED；二次无新增审计")
    void reApproveForbidden() throws Exception {
        mvc.perform(
                        post("/admin/onboarding/APP-M1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(SHOP_BODY))
                .andExpect(status().isOk());
        assertEquals(1, auditLogs.findByResourceId("APP-M1").size());

        mvc.perform(
                        post("/admin/onboarding/APP-M1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(SHOP_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("CAPABILITY_DENIED"));
        assertEquals(1, auditLogs.findByResourceId("APP-M1").size());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("OPERATOR 申请 APP-OP1 → 403 CAPABILITY_DENIED；无审计")
    void wrongCapabilityForbidden() throws Exception {
        mvc.perform(
                        post("/admin/onboarding/APP-OP1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(SHOP_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("CAPABILITY_DENIED"));
        assertTrue(auditLogs.findByResourceId("APP-OP1").isEmpty());
    }
}
