package com.evolutionary.credit.interfaces;

import static org.hamcrest.Matchers.startsWith;
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
 * 切片17a：月度出账 HTTP（AC-50 · OPEN Debt → Statement DUE）。
 *
 * <p>{@link DirtiesContext}：购信用写 OPEN debt / 出账改写账单，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class CreditMonthlyBillingHttpIT {

    private static final String PERIOD_BODY =
            "{\"periodStart\":\"2026-02-01T00:00:00Z\",\"periodEnd\":\"2026-02-28T23:59:59Z\"}";

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("purchase P-CREDIT-1 → monthly-billing → 200 stmt-UUID + totalDue 3000 DUE")
    void purchaseThenMonthlyBillingOk() throws Exception {
        mvc.perform(
                        post("/credit/purchases")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userId\":\"U1\",\"productId\":\"P-CREDIT-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debtId").exists());

        mvc.perform(
                        post("/credit/profiles/U1/monthly-billing")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(PERIOD_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", startsWith("stmt-")))
                .andExpect(jsonPath("$.userId").value("U1"))
                .andExpect(jsonPath("$.totalDue").value(3_000))
                .andExpect(jsonPath("$.status").value("DUE"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("无 OPEN debt → 422 CREDIT_NOT_AVAILABLE")
    void noOpenDebt422() throws Exception {
        mvc.perform(
                        post("/credit/profiles/U1/monthly-billing")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(PERIOD_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("CREDIT_NOT_AVAILABLE"));
    }
}
