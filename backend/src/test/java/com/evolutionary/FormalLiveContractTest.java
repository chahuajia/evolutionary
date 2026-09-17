package com.evolutionary;

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
 * 正式联调契约：FE RSC stations + credit 共用同一套 DevSeed。
 *
 * <p>种子来源：{@code DevSeedConfig}（S1/S2/S3）+ {@code CreditConfig}（U1）。
 */
@SpringBootTest
@AutoConfigureMockMvc
class FormalLiveContractTest {

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("GET /stations → ≥1 概览（DevSeed S1/S2/S3）")
    void stationsListHasSummaries() throws Exception {
        mvc.perform(get("/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    @DisplayName("GET /credit/profiles/U1 → 200（CreditConfig seed）")
    void creditProfileU1Ok() throws Exception {
        mvc.perform(get("/credit/profiles/U1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("U1"))
                .andExpect(jsonPath("$.creditLimit").value(10_000))
                .andExpect(jsonPath("$.usedCredit").value(3_000));
    }
}
