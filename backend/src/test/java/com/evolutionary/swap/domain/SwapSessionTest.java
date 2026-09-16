package com.evolutionary.swap.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.evolutionary.battery.domain.Battery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SwapSessionTest {

    private static Battery inUse(String id) {
        return Battery.create(id).swapOut();
    }

    private static Battery charging(String id) {
        return Battery.create(id).swapOut().returnForCharging();
    }

    @Test
    @DisplayName("记下站、换出、换进")
    void recordsTriple() {
        SwapSession session = SwapSession.record("S1", inUse("A"), charging("B"));
        assertEquals("S1", session.stationId());
        assertEquals("A", session.outgoing().id());
        assertEquals("B", session.incoming().id());
    }

    @Test
    @DisplayName("空白站 id 拒绝")
    void rejectsBlankStation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SwapSession.record(" ", inUse("A"), charging("B")));
    }

    @Test
    @DisplayName("同一块电池拒绝")
    void rejectsSameId() {
        Battery one = inUse("A");
        assertThrows(
                IllegalArgumentException.class,
                () -> SwapSession.record("S1", one, charging("A")));
    }

    @Test
    @DisplayName("outgoing 非 IN_USE 拒绝")
    void rejectsBadOutgoing() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SwapSession.record("S1", Battery.create("A"), charging("B")));
    }

    @Test
    @DisplayName("incoming 非 CHARGING 拒绝")
    void rejectsBadIncoming() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SwapSession.record("S1", inUse("A"), inUse("B")));
    }
}
