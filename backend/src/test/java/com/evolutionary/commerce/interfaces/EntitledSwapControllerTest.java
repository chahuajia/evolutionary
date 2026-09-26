package com.evolutionary.commerce.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EntitledSwapControllerTest {

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("POST /entitled-swaps 种子权益 → 200 + usageEventId/status")
    void happyPath() throws Exception {
        mvc.perform(
                        post("/entitled-swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U1\",\"entitlementId\":\"E-1\",\"cabinetId\":\"CAB-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usageEventId").exists())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("缺 userId → 400（边界 parse；entitlementId 可空走默认选卡）")
    void missingField400() throws Exception {
        mvc.perform(
                        post("/entitled-swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"entitlementId\":\"E-1\",\"cabinetId\":\"CAB-1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("userId required"));
    }

    @Test
    @DisplayName("权益用户不匹配 → 422 DomainOutcome.Err")
    void badEntitlementDomainErr() throws Exception {
        mvc.perform(
                        post("/entitled-swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U-other\",\"entitlementId\":\"E-1\",\"cabinetId\":\"CAB-1\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("ENTITLEMENT_INACTIVE"))
                .andExpect(jsonPath("$.suggestion").exists());
    }
}
