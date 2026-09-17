package com.evolutionary.mall.domain;

/** 商城域用例结果（无框架依赖）。 */
public sealed interface MallOutcome<T> permits MallOutcome.Ok, MallOutcome.Err {

    record Ok<T>(T value) implements MallOutcome<T> {}

    record Err<T>(MallErrorCode code, String message) implements MallOutcome<T> {}

    static <T> MallOutcome<T> ok(T value) {
        return new Ok<>(value);
    }

    static <T> MallOutcome<T> err(MallErrorCode code, String message) {
        return new Err<>(code, message);
    }
}
