package com.evolutionary.iot.interfaces;

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
 * 切片 11a：遥测入影 HTTP（AC-57）。
 *
 * <p>{@link DirtiesContext}：会写影子/遥测，避免污染同上下文其它测。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TelemetryHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("POST telemetry BAT-IOT-1 → 200 影子摘要；GET shadow soc=75 stale=false")
    void telemetryUpdatesShadow() throws Exception {
        mvc.perform(
                        post("/iot/batteries/BAT-IOT-1/telemetry")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"vendorId":"vendorA","soc":75,"voltageMilli":4150}
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteryId").value("BAT-IOT-1"))
                .andExpect(jsonPath("$.soc").value(75))
                .andExpect(jsonPath("$.voltageMilli").value(4150))
                .andExpect(jsonPath("$.stale").value(false))
                .andExpect(jsonPath("$.lastSeenAt").exists());

        mvc.perform(get("/iot/batteries/BAT-IOT-1/shadow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.soc").value(75))
                .andExpect(jsonPath("$.stale").value(false));
    }

    @Test
    @DisplayName("未知电池 telemetry → 404")
    void unknownBatteryNotFound() throws Exception {
        mvc.perform(
                        post("/iot/batteries/BAT-MISSING/telemetry")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"vendorId":"vendorA","soc":75,"voltageMilli":4150}
                                        """))
                .andExpect(status().isNotFound());
    }
}
