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
 * 切片29a：运营商批准 OPERATOR 下线入驻 HTTP（APP-DL1 / ORG-L1；禁 MERCHANT APP-M1）。
 *
 * <p>{@link DirtiesContext}：批准写 APPROVED + grant OPERATOR，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApproveOperatorDownlineHttpIT {

    private static final String ACTOR_BODY =
            "{\"actorUserId\":\"U-ADMIN\",\"actorOrgId\":\"ORG-L1\"}";

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("POST /operator/onboarding/APP-DL1/approve-downline → 200 ORG-DL1 OPERATOR")
    void approveAppDl1Ok() throws Exception {
        mvc.perform(
                        post("/operator/onboarding/APP-DL1/approve-downline")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(ACTOR_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orgId").value("ORG-DL1"))
                .andExpect(jsonPath("$.parentOrgId").value("ORG-L1"))
                .andExpect(jsonPath("$.operatorCapability").value(true));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("MERCHANT 申请 APP-M1 → 403 CAPABILITY_DENIED")
    void merchantCapabilityForbidden() throws Exception {
        mvc.perform(
                        post("/operator/onboarding/APP-M1/approve-downline")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(ACTOR_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("CAPABILITY_DENIED"));
    }
}
