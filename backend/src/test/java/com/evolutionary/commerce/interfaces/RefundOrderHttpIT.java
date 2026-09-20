package com.evolutionary.commerce.interfaces;

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
import org.springframework.test.web.servlet.MvcResult;

/**
 * 切片20a：退款 HTTP（信用购 P-CREDIT-1 → refund 200；再退 → 422 ORDER_NOT_REFUNDABLE）。
 *
 * <p>{@link DirtiesContext}：写单/退款改种子，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class RefundOrderHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("P-CREDIT-1 购 → refund 200 orderId/status/revokedEntitlementId；再退 422")
    void creditPurchaseThenRefund() throws Exception {
        MvcResult purchase =
                mvc.perform(
                                post("/credit/purchases")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"userId\":\"U1\",\"productId\":\"P-CREDIT-1\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.orderId").exists())
                        .andExpect(jsonPath("$.entitlementId").exists())
                        .andReturn();

        String body = purchase.getResponse().getContentAsString();
        String orderId = jsonField(body, "orderId");
        String entitlementId = jsonField(body, "entitlementId");

        mvc.perform(get("/commerce/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.userId").value("U1"))
                .andExpect(jsonPath("$.status").value("PAID"));

        mvc.perform(get("/commerce/orders/NO-SUCH")).andExpect(status().isNotFound());

        mvc.perform(post("/commerce/orders/" + orderId + "/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("REFUNDED"))
                .andExpect(jsonPath("$.revokedEntitlementId").value(entitlementId));

        mvc.perform(get("/commerce/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));

        mvc.perform(post("/commerce/orders/" + orderId + "/refund"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_REFUNDABLE"))
                .andExpect(jsonPath("$.suggestion").exists());
    }

    /** 最小 JSON 字段抽取（避免引入额外依赖）。 */
    private static String jsonField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start < 0) {
            throw new IllegalStateException("missing field: " + field + " in " + json);
        }
        start += key.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }
}
