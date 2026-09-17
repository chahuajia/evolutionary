package com.evolutionary.commerce.domain;

/**
 * 购买支付意图：期望扣减的积分与余额。
 *
 * <p>约束：usePointsCents + useBalanceCents 必须等于商品标价。
 */
public record PaymentIntent(long usePointsCents, long useBalanceCents) {

    public PaymentIntent {
        if (usePointsCents < 0 || useBalanceCents < 0) {
            throw new IllegalArgumentException("支付意图金额不能为负");
        }
    }

    /** 纯余额支付（回归路径）。 */
    public static PaymentIntent balanceOnly(Money price) {
        return new PaymentIntent(0, price.cents());
    }

    public boolean matchesPrice(Money price) {
        return usePointsCents + useBalanceCents == price.cents();
    }
}
