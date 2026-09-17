package com.evolutionary.credit.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.CreditProfileRepository;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.credit.domain.ScoreTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 切片9a：信用购 HTTP（P-CREDIT-1 · 200 orderId/entitlementId；额度不足 409）。
 *
 * <p>{@link DirtiesContext}：改写 usedCredit / 写单，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class CreditPurchaseHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("POST P-CREDIT-1 → 200 orderId + entitlementId + debtId")
    void creditPurchaseOk() throws Exception {
        mvc.perform(
                        post("/credit/purchases")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userId\":\"U1\",\"productId\":\"P-CREDIT-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.entitlementId").exists())
                .andExpect(jsonPath("$.debtId").exists())
                .andExpect(jsonPath("$.productId").value("P-CREDIT-1"))
                .andExpect(jsonPath("$.userId").value("U1"))
                .andExpect(jsonPath("$.paidAmountCents").value(3_000))
                .andExpect(jsonPath("$.usedCredit").value(6_000));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("额度不足 → 409 CREDIT_LIMIT_EXCEEDED")
    void creditLimitExceeded409(@Autowired CreditProfileRepository profiles) throws Exception {
        profiles.save(CreditProfile.open("U1", Money.cny(1_000), ScoreTier.B, 1));

        mvc.perform(
                        post("/credit/purchases")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userId\":\"U1\",\"productId\":\"P-CREDIT-1\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CREDIT_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.suggestion").exists());
    }
}
