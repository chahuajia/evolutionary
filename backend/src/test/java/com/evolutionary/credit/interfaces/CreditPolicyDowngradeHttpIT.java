package com.evolutionary.credit.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
 * 切片18a：apply-policy 政策降额 HTTP（AC-54）。
 *
 * <p>{@link DirtiesContext}：改写 U1 limit/policyVersion，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class CreditPolicyDowngradeHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("U1 limit=10000 v1 → apply v2 → 200 creditLimit=8000 policyVersion=2；usedCredit 不变")
    void applyPolicyV2UpdatesLimitAndVersion() throws Exception {
        mvc.perform(get("/credit/profiles/U1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditLimit").value(10_000))
                .andExpect(jsonPath("$.usedCredit").value(3_000))
                .andExpect(jsonPath("$.policyVersion").value(1));

        mvc.perform(
                        post("/credit/profiles/U1/apply-policy")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"policyVersion\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("U1"))
                .andExpect(jsonPath("$.creditLimit").value(8_000))
                .andExpect(jsonPath("$.usedCredit").value(3_000))
                .andExpect(jsonPath("$.policyVersion").value(2));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("未知政策版本 → 4xx")
    void unknownPolicyVersion4xx() throws Exception {
        mvc.perform(
                        post("/credit/profiles/U1/apply-policy")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"policyVersion\":99}"))
                .andExpect(status().is4xxClientError());
    }
}
