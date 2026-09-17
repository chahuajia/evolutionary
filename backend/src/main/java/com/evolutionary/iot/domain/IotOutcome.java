package com.evolutionary.iot.domain;

/** IoT 用例结果。 */
public sealed interface IotOutcome<T> permits IotOutcome.Ok, IotOutcome.Err {

    record Ok<T>(T value) implements IotOutcome<T> {}

    record Err<T>(IotErrorCode code, String message) implements IotOutcome<T> {}

    static <T> IotOutcome<T> ok(T value) {
        return new Ok<>(value);
    }

    static <T> IotOutcome<T> err(IotErrorCode code, String message) {
        return new Err<>(code, message);
    }
}
