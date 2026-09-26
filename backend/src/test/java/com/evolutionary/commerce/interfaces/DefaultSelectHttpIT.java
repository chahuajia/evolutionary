package com.evolutionary.commerce.interfaces;

import static org.hamcrest.Matchers.hasItem;
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
 * 切片12a：默认选卡 HTTP（AC-14 · 省略 entitlementId → 优先 FINITE）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DefaultSelectHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("GET ?userId=U1 → ACTIVE 目录含 E-FINITE / E-1")
    void listActiveEntitlementsForUser() throws Exception {
        mvc.perform(get("/entitled-swaps").param("userId", "U1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("E-FINITE")))
                .andExpect(jsonPath("$[*].id", hasItem("E-1")))
                .andExpect(jsonPath("$[?(@.id=='E-FINITE')].remainingSwaps").value(hasItem(5)))
                .andExpect(jsonPath("$[?(@.id=='E-1')].status").value(hasItem("ACTIVE")));
    }

    @Test
    @DisplayName("省略 entitlementId → 200 且选中 E-FINITE")
    void omitEntitlementIdSelectsFinite() throws Exception {
        mvc.perform(
                        post("/entitled-swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U1\",\"cabinetId\":\"CAB-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.entitlementId").value("E-FINITE"));
    }

    @Test
    @DisplayName("显式 E-1 仍可用")
    void explicitE1StillWorks() throws Exception {
        mvc.perform(
                        post("/entitled-swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U1\",\"entitlementId\":\"E-1\",\"cabinetId\":\"CAB-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entitlementId").value("E-1"));
    }
}
