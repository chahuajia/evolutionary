package com.evolutionary.operator.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 切片19a：批准商家入驻 HTTP（AC-40 · APP-M1 / ORG-NEW）。
 *
 * <p>{@link DirtiesContext}：批准写 APPROVED + MerchantProfile，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApproveMerchantOnboardingHttpIT {

    private static final String SHOP_BODY = "{\"shopName\":\"黑鸟旗舰店\"}";

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("POST /operator/onboarding/APP-M1/approve → 200 ORG-NEW ACTIVE")
    void approveAppM1Ok() throws Exception {
        mvc.perform(
                        post("/operator/onboarding/APP-M1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(SHOP_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantOrgId").value("ORG-NEW"))
                .andExpect(jsonPath("$.shopName").value("黑鸟旗舰店"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("重复批准 APP-M1 → 403 CAPABILITY_DENIED")
    void reApproveForbidden() throws Exception {
        mvc.perform(
                        post("/operator/onboarding/APP-M1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(SHOP_BODY))
                .andExpect(status().isOk());

        mvc.perform(
                        post("/operator/onboarding/APP-M1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(SHOP_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("CAPABILITY_DENIED"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("OPERATOR 申请 APP-OP1 → 403 CAPABILITY_DENIED")
    void wrongCapabilityForbidden() throws Exception {
        mvc.perform(
                        post("/operator/onboarding/APP-OP1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(SHOP_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("CAPABILITY_DENIED"));
    }
}
