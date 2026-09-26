package com.evolutionary.operator.interfaces;

import com.evolutionary.operator.domain.OperatorErrorCode;
import com.evolutionary.operator.domain.OperatorOutcome;
import org.springframework.http.HttpStatus;

/**
 * OperatorOutcome.Err → HTTP（S34：按错误码映射状态码；不嗅探 message）。
 */
public final class OperatorApiErrorTranslator {

    public record ApiError(String error, String suggestion) {}

    public record Translated(HttpStatus status, ApiError body) {}

    private OperatorApiErrorTranslator() {}

    public static <T> Translated translate(OperatorOutcome.Err<T> err) {
        OperatorErrorCode code = err.code();
        return new Translated(statusFor(code), new ApiError(code.name(), suggestionFor(code)));
    }

    private static HttpStatus statusFor(OperatorErrorCode code) {
        return switch (code) {
            case CAPABILITY_DENIED -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }

    private static String suggestionFor(OperatorErrorCode code) {
        return switch (code) {
            case CAPABILITY_DENIED ->
                    "approve only SUBMITTED MERCHANT applications; do not re-approve";
            case ORG_NOT_OWNER -> "use the owning organization for this resource";
            case ORG_NOT_DESCENDANT -> "operate within the ancestor org scope";
            case FIELD_NOT_OVERRIDABLE -> "override only fields listed on the template";
            case TEMPLATE_NOT_PUBLISHED -> "publish the package template first";
            case TEMPLATE_IMMUTABLE -> "create a new draft instead of mutating published base";
            case TEMPLATE_NOT_DRAFT -> "mutate only draft templates";
            case OVERRIDE_INVALID -> "fix override patches against overridable fields";
        };
    }
}
