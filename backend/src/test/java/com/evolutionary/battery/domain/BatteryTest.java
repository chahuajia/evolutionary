package com.evolutionary.battery.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link Battery} 的状态机测试。
 *
 * <p>按约定「测试名即行为」写：每个 {@code @DisplayName} 是一条可读的行为。
 */
class BatteryTest {

    private static Battery available() {
        return Battery.create("B-001");
    }

    @Test
    @DisplayName("新造的电池是 AVAILABLE")
    void newBatteryIsAvailable() {
        assertEquals(BatteryStatus.AVAILABLE, available().status());
    }

    @Test
    @DisplayName("id 为空时拒绝创建")
    void rejectsBlankId() {
        assertThrows(IllegalArgumentException.class, () -> Battery.create("  "));
    }

    @Nested
    @DisplayName("合法流转")
    class LegalTransitions {

        @Test
        @DisplayName("换出后变 IN_USE")
        void swapOut() {
            assertEquals(BatteryStatus.IN_USE, available().swapOut().status());
        }

        @Test
        @DisplayName("归还后变 CHARGING")
        void returnForCharging() {
            assertEquals(BatteryStatus.CHARGING, available().swapOut().returnForCharging().status());
        }

        @Test
        @DisplayName("充满后回到 AVAILABLE（一个完整循环）")
        void fullCycle() {
            Battery after = available().swapOut().returnForCharging().finishCharging();
            assertEquals(BatteryStatus.AVAILABLE, after.status());
        }

        @Test
        @DisplayName("流转返回新对象，原对象不变（不可变）")
        void transitionsAreImmutable() {
            Battery before = available();
            before.swapOut();
            assertEquals(BatteryStatus.AVAILABLE, before.status());
        }

        @Test
        @DisplayName("任何非终态都能退役")
        void retireFromCharging() {
            Battery charging = available().swapOut().returnForCharging();
            assertEquals(BatteryStatus.RETIRED, charging.retire().status());
        }
    }

    @Nested
    @DisplayName("非法流转一律拒绝")
    class IllegalTransitions {

        @Test
        @DisplayName("AVAILABLE 不能直接归还充电")
        void cannotReturnWhenAvailable() {
            assertThrows(
                    Battery.IllegalTransitionException.class,
                    () -> available().returnForCharging());
        }

        @Test
        @DisplayName("IN_USE 不能直接充满")
        void cannotFinishChargingWhileInUse() {
            assertThrows(
                    Battery.IllegalTransitionException.class,
                    () -> available().swapOut().finishCharging());
        }

        @Test
        @DisplayName("RETIRED 是终态：不能再换出")
        void retiredIsTerminal() {
            Battery retired = available().retire();
            assertThrows(Battery.IllegalTransitionException.class, retired::swapOut);
        }

        @Test
        @DisplayName("RETIRED 不能再次退役")
        void cannotRetireTwice() {
            Battery retired = available().retire();
            assertThrows(Battery.IllegalTransitionException.class, retired::retire);
        }
    }
}
