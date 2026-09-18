package com.evolutionary.commerce.interfaces;

import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import org.springframework.http.HttpStatus;

/**
 * Commerce DomainOutcome.Err → HTTP（S34：按错误码映射状态码）。
 *
 * <p>退款：ORDER_NOT_REFUNDABLE → 422；REFUND_BLOCKED_IN_PROGRESS_SWAP → 409。
 */
public final class CommerceApiErrorTranslator {

    public record ApiError(String error, String suggestion) {}

    public record Translated(HttpStatus status, ApiError body) {}

    private CommerceApiErrorTranslator() {}

    public static <T> Translated translate(DomainOutcome.Err<T> err) {
        DomainErrorCode code = err.code();
        return new Translated(statusFor(code), new ApiError(code.name(), suggestionFor(code)));
    }

    private static HttpStatus statusFor(DomainErrorCode code) {
        return switch (code) {
            case REFUND_BLOCKED_IN_PROGRESS_SWAP, BATTERY_NOT_AVAILABLE, BATTERY_ALREADY_RENTED,
                            CREDIT_OVERDUE_BLOCKED, TELEMETRY_STALE ->
                    HttpStatus.CONFLICT;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }

    private static String suggestionFor(DomainErrorCode code) {
        return switch (code) {
            case ORDER_NOT_REFUNDABLE -> "only PAID orders can be refunded";
            case REFUND_BLOCKED_IN_PROGRESS_SWAP -> "wait until the in-progress swap completes";
            case INSUFFICIENT_BALANCE -> "top up balance before retry";
            default -> null;
        };
    }
}
