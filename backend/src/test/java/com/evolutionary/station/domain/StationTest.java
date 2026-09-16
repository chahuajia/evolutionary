package com.evolutionary.station.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.battery.domain.BatteryStatus;
import com.evolutionary.station.domain.Station.NoAvailableBatteryException;
import com.evolutionary.station.domain.Station.Swap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StationTest {

    private static Battery available(String id) {
        return Battery.create(id);
    }

    private static Battery inUse(String id) {
        return Battery.create(id).swapOut();
    }

    @Nested
    @DisplayName("创建与在站清单")
    class Inventory {

        @Test
        @DisplayName("空站不能换出")
        void emptyCannotSwap() {
            Station station = Station.create("S1", "东门站");
            assertFalse(station.canSwapOut());
            assertThrows(NoAvailableBatteryException.class, () -> station.swap(inUse("B-in")));
        }

        @Test
        @DisplayName("空白 id / 名称拒绝创建")
        void rejectsBlank() {
            assertThrows(IllegalArgumentException.class, () -> Station.create(" ", "名"));
            assertThrows(IllegalArgumentException.class, () -> Station.create("S1", " "));
        }

        @Test
        @DisplayName("IN_USE 不能入站")
        void rejectsInUseOnRack() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> Station.create("S1", "东门站", java.util.List.of(inUse("B1"))));
        }

        @Test
        @DisplayName("重复电池 id 拒绝")
        void rejectsDuplicate() {
            Battery a = available("B1");
            Battery b = available("B1");
            assertThrows(
                    IllegalArgumentException.class,
                    () -> Station.create("S1", "东门站", java.util.List.of(a, b)));
        }
    }

    @Nested
    @DisplayName("合法换电")
    class LegalSwap {

        @Test
        @DisplayName("有可用电池时可以换出；记录站、出、进及完成后的状态")
        void swapsAndRecords() {
            Station station = Station.create("S1", "东门站", java.util.List.of(available("B-out")));
            Swap result = station.swap(inUse("B-in"));

            assertEquals(BatteryStatus.IN_USE, result.session().outgoing().status());
            assertEquals(BatteryStatus.CHARGING, result.session().incoming().status());
            assertEquals("S1", result.session().stationId());
            assertEquals(1, result.station().batteries().size());
            assertEquals(BatteryStatus.CHARGING, result.station().batteries().get(0).status());
            assertEquals("B-in", result.station().batteries().get(0).id());
        }

        @Test
        @DisplayName("换电不修改原 Station")
        void immutable() {
            Station before = Station.create("S1", "东门站", java.util.List.of(available("B-out")));
            before.swap(inUse("B-in"));
            assertEquals(1, before.batteries().size());
            assertEquals("B-out", before.batteries().get(0).id());
        }

        @Test
        @DisplayName("换出最后一块可用后不能再换")
        void lastAvailable() {
            Station station = Station.create("S1", "东门站", java.util.List.of(available("B-out")));
            Station after = station.swap(inUse("B-in")).station();
            assertFalse(after.canSwapOut());
            assertThrows(NoAvailableBatteryException.class, () -> after.swap(inUse("B-other")));
        }
    }

    @Nested
    @DisplayName("非法换电")
    class IllegalSwap {

        @Test
        @DisplayName("只有 CHARGING / RETIRED 时不能换出")
        void noAvailableStatuses() {
            Battery charging = available("C1").swapOut().returnForCharging();
            Battery retired = available("R1").retire();
            Station onlyCharging = Station.create("S1", "东门站", java.util.List.of(charging));
            Station onlyRetired = Station.create("S2", "西门站", java.util.List.of(retired));
            assertThrows(NoAvailableBatteryException.class, () -> onlyCharging.swap(inUse("X")));
            assertThrows(NoAvailableBatteryException.class, () -> onlyRetired.swap(inUse("Y")));
        }

        @Test
        @DisplayName("归还块不是 IN_USE → 电池非法流转")
        void incomingNotInUse() {
            Station station = Station.create("S1", "东门站", java.util.List.of(available("B-out")));
            assertThrows(
                    Battery.IllegalTransitionException.class,
                    () -> station.swap(available("B-in")));
        }

        @Test
        @DisplayName("归还块已在本站 → 拒绝")
        void incomingAlreadyHere() {
            Battery out = available("B-out");
            Battery parked = available("B-parked").swapOut().returnForCharging();
            Station station = Station.create("S1", "东门站", java.util.List.of(out, parked));
            assertThrows(IllegalArgumentException.class, () -> station.swap(inUse("B-parked")));
        }

        @Test
        @DisplayName("stock 后可用")
        void stockThenSwap() {
            Station empty = Station.create("S1", "东门站");
            Station stocked = empty.stock(available("B-out"));
            assertTrue(stocked.canSwapOut());
            Swap result = stocked.swap(inUse("B-in"));
            assertEquals("B-in", result.station().batteries().get(0).id());
        }
    }
}
