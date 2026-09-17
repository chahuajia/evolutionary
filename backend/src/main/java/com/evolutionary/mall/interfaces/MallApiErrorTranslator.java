package com.evolutionary.mall.interfaces;

import com.evolutionary.mall.domain.MallErrorCode;
import com.evolutionary.mall.domain.MallOutcome;
import org.springframework.http.HttpStatus;

/**
 * MallOutcome.Err → HTTP（S34：按错误码映射状态码；不嗅探 message）。
 */
public final class MallApiErrorTranslator {

    public record ApiError(String error, String suggestion) {}

    public record Translated(HttpStatus status, ApiError body) {}

    private MallApiErrorTranslator() {}

    public static <T> Translated translate(MallOutcome.Err<T> err) {
        MallErrorCode code = err.code();
        return new Translated(statusFor(code), new ApiError(code.name(), suggestionFor(code)));
    }

    private static HttpStatus statusFor(MallErrorCode code) {
        return switch (code) {
            case CAMPAIGN_BUDGET_EXHAUSTED -> HttpStatus.CONFLICT;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }

    private static String suggestionFor(MallErrorCode code) {
        return switch (code) {
            case CAMPAIGN_BUDGET_EXHAUSTED -> "wait for campaign budget refill or try another campaign";
            case CAMPAIGN_NOT_ACTIVE -> "use an active campaign that includes the template";
            case COUPON_NOT_AVAILABLE -> "check template id or coupon availability";
            default -> null;
        };
    }
}
