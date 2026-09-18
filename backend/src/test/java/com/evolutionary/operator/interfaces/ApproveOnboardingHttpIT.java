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
 * 切片19a：商家入驻批准 HTTP（AC-40 · APP-M1）。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApproveOnboardingHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("POST approve APP-M1 → 200 ACTIVE ORG-M1")
    void approveOk() throws Exception {
        mvc.perform(
                        post("/operator/onboarding/APP-M1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"shopName\":\"黑鸟旗舰店\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantOrgId").value("ORG-M1"))
                .andExpect(jsonPath("$.shopName").value("黑鸟旗舰店"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("重复批准 → 4xx")
    void approveTwiceFails() throws Exception {
        mvc.perform(
                        post("/operator/onboarding/APP-M1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"shopName\":\"黑鸟旗舰店\"}"))
                .andExpect(status().isOk());

        mvc.perform(
                        post("/operator/onboarding/APP-M1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"shopName\":\"黑鸟旗舰店\"}"))
                .andExpect(status().is4xxClientError());
    }
}
