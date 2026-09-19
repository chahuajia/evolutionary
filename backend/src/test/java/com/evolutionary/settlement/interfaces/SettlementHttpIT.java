package com.evolutionary.settlement.interfaces;

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
 * 切片21a：accrue → runBatch（AC-34）；accrue → reverse（AC-35）。
 *
 * <p>契约对齐 SettlementController：orgId / amountCents；batch 响应字段 id。
 */
@SpringBootTest
@AutoConfigureMockMvc
class SettlementHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("accrue → batches → CLOSED")
    void accrueThenBatch() throws Exception {
        mvc.perform(
                        post("/settlement/accruals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "orderId":"O-STL-1",
                                          "userId":"U1",
                                          "orgId":"ORG-L2",
                                          "amountCents":10000,
                                          "currency":"CNY",
                                          "completedAt":"2026-09-18T00:00:00Z"
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value("O-STL-1"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        mvc.perform(
                        post("/settlement/batches")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "periodStart":"2026-09-17T00:00:00Z",
                                          "periodEnd":"2026-09-19T00:00:00Z"
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("accrue → reverse-accruals → REVERSED（AC-35）")
    void accrueThenReverse() throws Exception {
        mvc.perform(
                        post("/settlement/accruals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "orderId":"O-STL-2",
                                          "orgId":"ORG-L2",
                                          "amountCents":5000,
                                          "completedAt":"2026-09-18T01:00:00Z"
                                        }
                                        """))
                .andExpect(status().isOk());

        mvc.perform(post("/settlement/orders/O-STL-2/reverse-accruals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("REVERSED"));

        // ── 读侧：GET 必须能看到 REVERSED ─────────────────────
        // 这是本接口存在的理由：只返 PENDING 的话，
        // 界面上永远没有"为什么这条不能动"需要解释的东西。
        mvc.perform(get("/settlement/accruals").param("orgId", "ORG-L2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("REVERSED"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("GET /settlement/accruals：缺 orgId → 400")
    void listRequiresOrgId() throws Exception {
        mvc.perform(get("/settlement/accruals")).andExpect(status().isBadRequest());
        mvc.perform(get("/settlement/accruals").param("orgId", "  "))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("GET /settlement/accruals：未知组织 → 空列表（不是 404）")
    void listUnknownOrgIsEmpty() throws Exception {
        // 空列表与 404 的区别很重要：**"这个组织没有意向"是合法答案**，
        // 不是错误。返回 404 会逼前端把"没数据"和"查错了"混为一谈。
        mvc.perform(get("/settlement/accruals").param("orgId", "ORG-NOPE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
