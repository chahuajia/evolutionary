package com.evolutionary.commerce.interfaces;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 切片27b：钱包读 HTTP（U1 种子 → 200；未知用户 → 404）。
 */
@SpringBootTest
@AutoConfigureMockMvc
class WalletHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("U1 → 200 balanceCents/pointsCents ≥0 currency=CNY")
    void u1WalletOk() throws Exception {
        mvc.perform(get("/commerce/users/U1/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("U1"))
                .andExpect(jsonPath("$.balanceCents").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.pointsCents").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.currency").value("CNY"));
    }

    @Test
    @DisplayName("未知用户 → 404")
    void unknownUserNotFound() throws Exception {
        mvc.perform(get("/commerce/users/U-UNKNOWN/wallet"))
                .andExpect(status().isNotFound());
    }
}
