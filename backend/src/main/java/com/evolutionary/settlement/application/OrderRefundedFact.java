package com.evolutionary.settlement.application;

import java.time.Instant;

/** 订单退款事实（settlement 侧输入；不改 commerce Order 聚合）。 */
public record OrderRefundedFact(String orderId, Instant refundedAt) {

    public OrderRefundedFact {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        if (refundedAt == null) {
            throw new IllegalArgumentException("refundedAt 不能为空");
        }
    }
}
