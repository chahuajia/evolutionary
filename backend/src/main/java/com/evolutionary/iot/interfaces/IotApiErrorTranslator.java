package com.evolutionary.iot.interfaces;

import com.evolutionary.iot.domain.IotErrorCode;
import com.evolutionary.iot.domain.IotOutcome;
import org.springframework.http.HttpStatus;

/**
 * IotOutcome.Err → HTTP（S34：按错误码映射状态码；不嗅探 message）。
 */
public final class IotApiErrorTranslator {

    public record ApiError(String error, String suggestion) {}

    public record Translated(HttpStatus status, ApiError body) {}

    private IotApiErrorTranslator() {}

    public static <T> Translated translate(IotOutcome.Err<T> err) {
        IotErrorCode code = err.code();
        return new Translated(statusFor(code), new ApiError(code.name(), suggestionFor(code)));
    }

    private static HttpStatus statusFor(IotErrorCode code) {
        return switch (code) {
            case TELEMETRY_STALE -> HttpStatus.CONFLICT;
            case COMMAND_FAILED, UNKNOWN_VENDOR, PARSE_ERROR -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }

    private static String suggestionFor(IotErrorCode code) {
        return switch (code) {
            case TELEMETRY_STALE -> "wait for fresh telemetry or run detect-comm-lost triage";
            case COMMAND_FAILED -> "retry the command or check device connectivity";
            case UNKNOWN_VENDOR -> "register the vendor adapter before commanding";
            case PARSE_ERROR -> "fix the vendor payload or adapter parse path";
        };
    }
}
