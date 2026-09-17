package com.evolutionary.commerce.domain;

import java.util.Objects;

/** 金额以整数分存储，避免浮点。 */
public record Money(long cents, Currency currency) {

    public Money {
        if (cents < 0) {
            throw new IllegalArgumentException("cents must not be negative");
        }
        Objects.requireNonNull(currency, "currency");
    }

    public static Money cny(long cents) {
        return new Money(cents, Currency.CNY);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(cents + other.cents, currency);
    }

    public boolean covers(Money price) {
        requireSameCurrency(price);
        return cents >= price.cents;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other");
        if (currency != other.currency) {
            throw new IllegalArgumentException("currency mismatch");
        }
    }
}
