package com.evolutionary.swap.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.evolutionary.swap.application.UnknownStationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class SwapApiErrorTranslatorTest {

    @Test
    @DisplayName("未知站 → 404，透传 message")
    void unknownStation() {
        var t = SwapApiErrorTranslator.translate(new UnknownStationException("S-missing"));
        assertEquals(HttpStatus.NOT_FOUND, t.status());
        assertEquals("unknown station: S-missing", t.body().error());
        assertNull(t.body().suggestion());
    }

    @Test
    @DisplayName("缺字段 → 400")
    void badRequest() {
        var t = SwapApiErrorTranslator.translate(new IllegalArgumentException("incomingBatteryId required"));
        assertEquals(HttpStatus.BAD_REQUEST, t.status());
        assertEquals("incomingBatteryId required", t.body().error());
    }
}
