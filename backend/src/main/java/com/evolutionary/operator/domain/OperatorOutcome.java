package com.evolutionary.operator.domain;

/** 运营域用例结果（无框架依赖）。 */
public sealed interface OperatorOutcome<T> permits OperatorOutcome.Ok, OperatorOutcome.Err {

    record Ok<T>(T value) implements OperatorOutcome<T> {}

    record Err<T>(OperatorErrorCode code, String message) implements OperatorOutcome<T> {}

    static <T> OperatorOutcome<T> ok(T value) {
        return new Ok<>(value);
    }

    static <T> OperatorOutcome<T> err(OperatorErrorCode code, String message) {
        return new Err<>(code, message);
    }
}
