package com.evolutionary.iot.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 切片13a：SOC 过时诊断 HTTP（AC-61）。
 *
 * <p>{@link DirtiesContext}：会写影子；@Order 保证先 stale 再 telemetry 刷新。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TriageOutdatedSocHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @Order(1)
    @DisplayName("种子 BAT-IOT-1 stale → SHADOW_STALE")
    void staleSeedYieldsShadowStale() throws Exception {
        mvc.perform(post("/iot/batteries/BAT-IOT-1/triage-outdated-soc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteryId").value("BAT-IOT-1"))
                .andExpect(jsonPath("$.nextStep").value("SHADOW_STALE"))
                .andExpect(jsonPath("$.stale").value(true))
                .andExpect(jsonPath("$.soc").exists())
                .andExpect(jsonPath("$.lastSeenAt").exists())
                .andExpect(jsonPath("$.orderedChecks").isArray())
                .andExpect(jsonPath("$.orderedChecks[0]").value("检查 shadow.stale 与 lastSeenAt"));
    }

    @Test
    @Order(2)
    @DisplayName("telemetry 刷新后再 triage → CHECK_ADAPTER")
    void freshShadowYieldsCheckAdapter() throws Exception {
        mvc.perform(
                        post("/iot/batteries/BAT-IOT-1/telemetry")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"vendorId":"vendorA","soc":80,"voltageMilli":4100}
                                        """))
                .andExpect(status().isOk());

        mvc.perform(post("/iot/batteries/BAT-IOT-1/triage-outdated-soc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextStep").value("CHECK_ADAPTER"))
                .andExpect(jsonPath("$.stale").value(false))
                .andExpect(jsonPath("$.soc").value(80));
    }

    @Test
    @Order(3)
    @DisplayName("未知电池 → 404")
    void unknownNotFound() throws Exception {
        mvc.perform(post("/iot/batteries/BAT-MISSING/triage-outdated-soc"))
                .andExpect(status().isNotFound());
    }
}
