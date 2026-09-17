package com.evolutionary.iot.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 切片 8a：detect-comm-lost → COMM_LOST；再 detect 不重复开单。
 *
 * <p>{@link DirtiesContext}：会写告警/工单，避免污染同上下文其它测。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CommLostHttpIT {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("GET shadow BAT-IOT-1 → 200；detect → COMM_LOST；再 detect 同 ticketId")
    void detectCommLostRaisesOnce() throws Exception {
        mvc.perform(get("/iot/batteries/BAT-IOT-1/shadow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteryId").value("BAT-IOT-1"));

        MvcResult first =
                mvc.perform(post("/iot/batteries/BAT-IOT-1/detect-comm-lost"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.batteryId").value("BAT-IOT-1"))
                        .andExpect(jsonPath("$.stale").value(true))
                        .andExpect(jsonPath("$.raised").value(true))
                        .andExpect(jsonPath("$.alertType").value("COMM_LOST"))
                        .andExpect(jsonPath("$.ticketId").exists())
                        .andReturn();

        JsonNode body = MAPPER.readTree(first.getResponse().getContentAsString());
        String ticketId = body.get("ticketId").asText();

        mvc.perform(post("/iot/batteries/BAT-IOT-1/detect-comm-lost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stale").value(true))
                .andExpect(jsonPath("$.raised").value(true))
                .andExpect(jsonPath("$.alertType").value("COMM_LOST"))
                .andExpect(jsonPath("$.ticketId").value(ticketId));
    }

    @Test
    @DisplayName("未知电池 shadow / detect → 404")
    void unknownBatteryNotFound() throws Exception {
        mvc.perform(get("/iot/batteries/BAT-MISSING/shadow")).andExpect(status().isNotFound());
        mvc.perform(post("/iot/batteries/BAT-MISSING/detect-comm-lost"))
                .andExpect(status().isNotFound());
    }
}
