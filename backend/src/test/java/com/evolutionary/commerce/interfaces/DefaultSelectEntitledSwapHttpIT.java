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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 切片12a：默认选卡 HTTP（省略 entitlementId → executeWithoutId · AC-14 优先 FINITE）。
 *
 * <p>{@link DirtiesContext}：默认选卡消耗 E-FINITE remainingSwaps，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class DefaultSelectEntitledSwapHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("省略 entitlementId → 200 且 entitlementId=E-FINITE（AC-14）")
    void omitEntitlementIdSelectsFinite() throws Exception {
        mvc.perform(
                        post("/entitled-swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userId\":\"U1\",\"cabinetId\":\"CAB-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.entitlementId").value("E-FINITE"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("显式 entitlementId=E-1 仍可用")
    void explicitUnlimitedStillWorks() throws Exception {
        mvc.perform(
                        post("/entitled-swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U1\",\"entitlementId\":\"E-1\",\"cabinetId\":\"CAB-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.entitlementId").value("E-1"));
    }
}
