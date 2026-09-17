package com.evolutionary.swap.interfaces;

import com.evolutionary.station.domain.Station.NoAvailableBatteryException;
import com.evolutionary.swap.application.UnknownStationException;
import org.springframework.http.HttpStatus;

/**
 * 领域/应用异常 → HTTP（S34：透传 message，边界只加 suggestion/状态码）。
 * 见 specs/round-14.md。
 */
public final class SwapApiErrorTranslator {

    public record ApiError(String error, String suggestion) {}

    public record Translated(HttpStatus status, ApiError body) {}

    private SwapApiErrorTranslator() {}

    public static Translated translate(RuntimeException ex) {
        if (ex instanceof UnknownStationException) {
            return new Translated(HttpStatus.NOT_FOUND, new ApiError(ex.getMessage(), null));
        }
        if (ex instanceof NoAvailableBatteryException) {
            return new Translated(
                    HttpStatus.CONFLICT,
                    new ApiError(
                            ex.getMessage(),
                            "wait until a battery finishes charging or restock the station"));
        }
        if (ex instanceof IllegalArgumentException) {
            String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
            return new Translated(HttpStatus.BAD_REQUEST, new ApiError(msg, null));
        }
        throw ex;
    }
}
