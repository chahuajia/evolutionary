package com.evolutionary.mall.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 切片14a：商城下单 HTTP（AC-41 · S1 / M1 · 1000¢）。
 */
@SpringBootTest
@AutoConfigureMockMvc
class PurchaseMallOrderHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("POST /mall/orders S1 qty=1 → 200 PAID + paidAmountCents=1000")
    void purchaseOk() throws Exception {
        mvc.perform(
                        post("/mall/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U1\",\"merchantOrgId\":\"M1\",\"skuId\":\"S1\",\"qty\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAmountCents").value(1000))
                .andExpect(jsonPath("$.skuId").value("S1"))
                .andExpect(jsonPath("$.qty").value(1));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("余额不足 → 422 INSUFFICIENT_BALANCE")
    void insufficientBalance422(@Autowired AccountRepository accounts) throws Exception {
        accounts.save(
                Account.open(
                        "ACC-U1-BAL",
                        AccountOwnerType.USER,
                        "U1",
                        AccountType.BALANCE,
                        Currency.CNY,
                        100));

        mvc.perform(
                        post("/mall/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U1\",\"merchantOrgId\":\"M1\",\"skuId\":\"S1\",\"qty\":1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_BALANCE"));
    }
}
