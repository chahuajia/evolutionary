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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 切片15a：运维工单列表 HTTP（detect-comm-lost 后可查）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MaintenanceTicketsHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("detect-comm-lost 后 GET tickets → 含 OPEN COMM_LOST")
    void listTicketsAfterDetect() throws Exception {
        mvc.perform(post("/iot/batteries/BAT-IOT-1/detect-comm-lost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.raised").value(true));

        mvc.perform(get("/iot/batteries/BAT-IOT-1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].batteryId").value("BAT-IOT-1"))
                .andExpect(jsonPath("$[0].alertType").value("COMM_LOST"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].ticketId").exists());
    }

    @Test
    @DisplayName("未知电池 → 404")
    void unknownNotFound() throws Exception {
        mvc.perform(get("/iot/batteries/BAT-MISSING/tickets"))
                .andExpect(status().isNotFound());
    }
}
