package com.evolutionary.credit.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.CreditProfileRepository;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.credit.domain.ScoreTier;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 切片9a / 22a：信用购 HTTP（P-CREDIT-1 · 200；购后自动 PENDING accrual；额度不足 409）。
 *
 * <p>{@link DirtiesContext}：改写 usedCredit / 写单，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class CreditPurchaseHttpIT {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

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
    @DisplayName("购后自动 accrue：reverse 非空 REVERSED（证明 PENDING 已记）")
    void creditPurchaseAutoAccruesPending() throws Exception {
        MvcResult purchase =
                mvc.perform(
                                post("/credit/purchases")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"userId\":\"U1\",\"productId\":\"P-CREDIT-1\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.orderId").exists())
                        .andReturn();
        String orderId =
                objectMapper
                        .readTree(purchase.getResponse().getContentAsString())
                        .get("orderId")
                        .asText();

        mvc.perform(post("/settlement/orders/" + orderId + "/reverse-accruals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(orderId))
                .andExpect(jsonPath("$[0].status").value("REVERSED"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("购后自动 accrue → batches CLOSED")
    void creditPurchaseThenSettlementBatch() throws Exception {
        Instant now = Instant.now();
        String periodStart = now.minus(1, ChronoUnit.DAYS).toString();
        String periodEnd = now.plus(1, ChronoUnit.DAYS).toString();

        mvc.perform(
                        post("/credit/purchases")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userId\":\"U1\",\"productId\":\"P-CREDIT-1\"}"))
                .andExpect(status().isOk());

        mvc.perform(
                        post("/settlement/batches")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "periodStart":"%s",
                                          "periodEnd":"%s"
                                        }
                                        """
                                                .formatted(periodStart, periodEnd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.id").exists());
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
