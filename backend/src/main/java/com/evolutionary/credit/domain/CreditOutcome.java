package com.evolutionary.credit.domain;

/** 信用域用例结果。 */
public sealed interface CreditOutcome<T> permits CreditOutcome.Ok, CreditOutcome.Err {

    record Ok<T>(T value) implements CreditOutcome<T> {}

    record Err<T>(CreditErrorCode code, String message) implements CreditOutcome<T> {}

    static <T> CreditOutcome<T> ok(T value) {
        return new Ok<>(value);
    }

    static <T> CreditOutcome<T> err(CreditErrorCode code, String message) {
        return new Err<>(code, message);
    }
}
