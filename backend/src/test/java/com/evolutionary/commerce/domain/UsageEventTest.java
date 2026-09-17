package com.evolutionary.commerce.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UsageEventTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");

    @Test
    @DisplayName("start 后是 STARTED")
    void start() {
        UsageEvent event = UsageEvent.start("UE-1", "U-1", "E-1", "BAT-1", "CAB-1", T0);
        assertTrue(event.isStarted());
    }

    @Test
    @DisplayName("STARTED 可完成")
    void complete() {
        UsageEvent completed =
                UsageEvent.start("UE-1", "U-1", "E-1", "BAT-1", "CAB-1", T0).complete(T0.plusSeconds(10));
        assertEquals(UsageEventStatus.COMPLETED, completed.status());
    }

    @Test
    @DisplayName("COMPLETED 不能再 complete")
    void cannotCompleteTwice() {
        UsageEvent completed =
                UsageEvent.start("UE-1", "U-1", "E-1", "BAT-1", "CAB-1", T0).complete(T0);
        assertThrows(UsageEvent.IllegalTransitionException.class, () -> completed.complete(T0));
    }
}
