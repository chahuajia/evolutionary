package com.evolutionary.credit.interfaces;

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
 * 切片5：mark-overdue 冻 E-1 后 POST /entitled-swaps → 409 CREDIT_OVERDUE_BLOCKED。
 *
 * <p>{@link DirtiesContext}：本类会改写种子 U1/E-1，避免污染同上下文的只读/换电测。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CreditOverdueHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("mark-overdue U1 → 200 overdue；再 POST /entitled-swaps → 409 CREDIT_OVERDUE_BLOCKED")
    void markOverdueThenEntitledSwapBlocked() throws Exception {
        mvc.perform(
                        post("/credit/profiles/U1/mark-overdue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statementId\":\"STMT-2026-02\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("U1"))
                .andExpect(jsonPath("$.status").value("overdue"));

        mvc.perform(
                        post("/entitled-swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U1\",\"entitlementId\":\"E-1\",\"cabinetId\":\"CAB-1\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CREDIT_OVERDUE_BLOCKED"))
                .andExpect(jsonPath("$.suggestion").exists());
    }
}
