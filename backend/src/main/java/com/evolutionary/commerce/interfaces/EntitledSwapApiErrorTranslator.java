package com.evolutionary.commerce.interfaces;

import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import org.springframework.http.HttpStatus;

/**
 * DomainOutcome.Err → HTTP（S34：按错误码映射状态码；边界可附 suggestion）。
 *
 * <p>不把 Spring 类型渗入领域；翻译只发生在 interfaces。
 */
public final class EntitledSwapApiErrorTranslator {

    public record ApiError(String error, String suggestion) {}

    public record Translated(HttpStatus status, ApiError body) {}

    private EntitledSwapApiErrorTranslator() {}

    public static <T> Translated translate(DomainOutcome.Err<T> err) {
        DomainErrorCode code = err.code();
        return new Translated(statusFor(code), new ApiError(code.name(), suggestionFor(code)));
    }

    private static HttpStatus statusFor(DomainErrorCode code) {
        return switch (code) {
            case BATTERY_NOT_AVAILABLE, BATTERY_ALREADY_RENTED, CREDIT_OVERDUE_BLOCKED, TELEMETRY_STALE,
                            REFUND_BLOCKED_IN_PROGRESS_SWAP ->
                    HttpStatus.CONFLICT;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }

    private static String suggestionFor(DomainErrorCode code) {
        return switch (code) {
            case BATTERY_NOT_AVAILABLE -> "wait for an idle battery or restock";
            case BATTERY_ALREADY_RENTED -> "complete or cancel the open usage first";
            case ENTITLEMENT_INACTIVE -> "use an active entitlement for this user";
            case ENTITLEMENT_EXPIRED -> "renew or purchase a new entitlement";
            case ENTITLEMENT_EXHAUSTED -> "purchase more swaps or another product";
            case CREDIT_OVERDUE_BLOCKED -> "settle overdue credit to unfreeze entitlement";
            case INSUFFICIENT_BALANCE -> "top up balance before metered swap";
            default -> null;
        };
    }
}
