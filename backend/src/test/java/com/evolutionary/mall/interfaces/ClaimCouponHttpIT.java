package com.evolutionary.mall.interfaces;

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
 * 切片10a：活动领券 HTTP（CAMP-OK · 200 id/userId/templateId/status；预算耗尽 409）。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ClaimCouponHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("POST CAMP-OK → 200 id + userId + templateId + status=available")
    void claimOk() throws Exception {
        mvc.perform(
                        post("/mall/campaigns/CAMP-OK/claims")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userId\":\"U1\",\"templateId\":\"T-C1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value("U1"))
                .andExpect(jsonPath("$.templateId").value("T-C1"))
                .andExpect(jsonPath("$.status").value("available"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("CAMP-EMPTY 预算耗尽 → 409 CAMPAIGN_BUDGET_EXHAUSTED")
    void budgetExhausted409() throws Exception {
        mvc.perform(
                        post("/mall/campaigns/CAMP-EMPTY/claims")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userId\":\"U1\",\"templateId\":\"T-C1\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CAMPAIGN_BUDGET_EXHAUSTED"))
                .andExpect(jsonPath("$.suggestion").exists());
    }
}
