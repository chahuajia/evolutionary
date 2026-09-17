package com.evolutionary.settlement.application;

import java.time.Instant;

/**
 * 订单完成事实（settlement 侧输入）。
 *
 * <p>不进入 commerce 交易聚合内部；仅携带 OrderId 与分润所需字段。
 */
public record OrderCompletedFact(
        String orderId,
        String userId,
        String sellerOrgId,
        long paidAmountCents,
        String currency,
        Instant completedAt) {

    public OrderCompletedFact {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (sellerOrgId == null || sellerOrgId.isBlank()) {
            throw new IllegalArgumentException("sellerOrgId 不能为空");
        }
        if (paidAmountCents < 0) {
            throw new IllegalArgumentException("金额不能为负");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency 不能为空");
        }
        if (completedAt == null) {
            throw new IllegalArgumentException("completedAt 不能为空");
        }
    }
}
