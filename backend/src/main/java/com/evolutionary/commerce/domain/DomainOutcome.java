package com.evolutionary.commerce.domain;

public sealed interface DomainOutcome<T> permits DomainOutcome.Ok, DomainOutcome.Err {

    record Ok<T>(T value) implements DomainOutcome<T> {}

    record Err<T>(DomainErrorCode code, String message) implements DomainOutcome<T> {}

    static <T> DomainOutcome<T> ok(T value) {
        return new Ok<>(value);
    }

    static <T> DomainOutcome<T> err(DomainErrorCode code, String message) {
        return new Err<>(code, message);
    }
}
