package com.evolutionary.commerce.interfaces;

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
 * 切片7：计量权益换电 HTTP（P-M1 / E-M1 · soc 80→60 → 1000¢；余额不足 422）。
 *
 * <p>{@link DirtiesContext}：改写余额/消耗电池种子，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class MeteredEntitledSwapHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("POST E-M1 + soc 80/60 → 200 COMPLETED + chargedAmountCents=1000")
    void meteredSwapCharges1000() throws Exception {
        mvc.perform(
                        post("/entitled-swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U1\",\"entitlementId\":\"E-M1\",\"cabinetId\":\"CAB-1\",\"socBefore\":80,\"socAfter\":60}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.entitlementId").value("E-M1"))
                .andExpect(jsonPath("$.chargedAmountCents").value(1000));
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
                        post("/entitled-swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U1\",\"entitlementId\":\"E-M1\",\"cabinetId\":\"CAB-1\",\"socBefore\":80,\"socAfter\":60}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_BALANCE"))
                .andExpect(jsonPath("$.suggestion").exists());
    }
}
