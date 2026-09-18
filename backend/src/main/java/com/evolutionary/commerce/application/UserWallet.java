package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Currency;

/** 消费者钱包读模型（余额 + 积分，单位均为分）。 */
public record UserWallet(
        String userId, long balanceCents, long pointsCents, Currency currency) {}
