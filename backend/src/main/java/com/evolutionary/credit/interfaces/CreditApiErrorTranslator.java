package com.evolutionary.credit.interfaces;

import com.evolutionary.credit.domain.CreditErrorCode;
import com.evolutionary.credit.domain.CreditOutcome;
import org.springframework.http.HttpStatus;

/**
 * CreditOutcome.Err → HTTP（S34：按错误码映射状态码；不嗅探 message）。
 */
public final class CreditApiErrorTranslator {

    public record ApiError(String error, String suggestion) {}

    public record Translated(HttpStatus status, ApiError body) {}

    private CreditApiErrorTranslator() {}

    public static <T> Translated translate(CreditOutcome.Err<T> err) {
        CreditErrorCode code = err.code();
        return new Translated(statusFor(code), new ApiError(code.name(), suggestionFor(code)));
    }

    private static HttpStatus statusFor(CreditErrorCode code) {
        return switch (code) {
            case CREDIT_OVERDUE_BLOCKED, CREDIT_PROFILE_FROZEN, CREDIT_LIMIT_EXCEEDED ->
                    HttpStatus.CONFLICT;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }

    private static String suggestionFor(CreditErrorCode code) {
        return switch (code) {
            case STATEMENT_NOT_DUE -> "use a DUE statement that is past its dueDate";
            case CREDIT_OVERDUE_BLOCKED -> "settle overdue credit before new purchases";
            case CREDIT_PROFILE_FROZEN -> "contact support or repay to unfreeze profile";
            case CREDIT_LIMIT_EXCEEDED -> "repay outstanding credit or request a higher limit";
            case PARTIAL_REPAY_NOT_ALLOWED -> "repay the full statement totalDue in one payment";
            case CREDIT_NOT_AVAILABLE -> "top up user balance to cover the statement totalDue";
            case INSUFFICIENT_BALANCE -> "top up user balance before repay";
            default -> null;
        };
    }
}
