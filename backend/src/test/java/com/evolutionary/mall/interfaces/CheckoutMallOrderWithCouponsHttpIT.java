package com.evolutionary.mall.interfaces;

import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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
 * 切片16a：带券结账 HTTP（AC-42..44 · claim CAMP-OK → checkout S1 · T-C1 FIXED_OFF 500）。
 *
 * <p>种子 S1=1000¢、T-C1 minSpend=3000¢ → qty=3 满足门槛；原价 3000¢ − 500¢ = paid 2500¢。
 */
@SpringBootTest
@AutoConfigureMockMvc
class CheckoutMallOrderWithCouponsHttpIT {

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("claim CAMP-OK → checkout S1 qty=3 带券 → 200 PAID paid < 原价")
    void claimThenCheckoutWithCoupon() throws Exception {
        MvcResult claim =
                mvc.perform(
                                post("/mall/campaigns/CAMP-OK/claims")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"userId\":\"U1\",\"templateId\":\"T-C1\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").exists())
                        .andReturn();
        String couponId = JsonPath.read(claim.getResponse().getContentAsString(), "$.id");

        long originalCents = 3_000L;
        mvc.perform(
                        post("/mall/orders/checkout-with-coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U1\",\"merchantOrgId\":\"M1\",\"skuId\":\"S1\",\"qty\":3,\"userCouponIds\":[\""
                                                + couponId
                                                + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAmountCents").value(2_500))
                .andExpect(jsonPath("$.paidAmountCents", lessThan((int) originalCents)))
                .andExpect(jsonPath("$.discountCents").value(500))
                .andExpect(jsonPath("$.skuId").value("S1"))
                .andExpect(jsonPath("$.qty").value(3));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("无券列表 → 等价无券路径 paidAmountCents=1000")
    void emptyCouponsEqualsNoCouponPath() throws Exception {
        mvc.perform(
                        post("/mall/orders/checkout-with-coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"userId\":\"U1\",\"merchantOrgId\":\"M1\",\"skuId\":\"S1\",\"qty\":1,\"userCouponIds\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAmountCents").value(1000))
                .andExpect(jsonPath("$.discountCents").value(0));
    }
}
