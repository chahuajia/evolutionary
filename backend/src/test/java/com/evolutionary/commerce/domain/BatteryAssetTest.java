package com.evolutionary.commerce.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BatteryAssetTest {

    @Test
    @DisplayName("checkout 后变 RENTED 并记录持有人")
    void checkout() {
        BatteryAsset rented =
                BatteryAsset.createIdle("BAT-1", "ORG-1", "v", "m").checkout("U-1");
        assertEquals(BatteryAssetStatus.RENTED, rented.status());
        assertEquals("U-1", rented.currentHolderId());
    }

    @Test
    @DisplayName("归还后变 IDLE 并清除持有人")
    void returnToIdle() {
        BatteryAsset idle =
                BatteryAsset.createIdle("BAT-1", "ORG-1", "v", "m").checkout("U-1").returnToIdle();
        assertEquals(BatteryAssetStatus.IDLE, idle.status());
        assertNull(idle.currentHolderId());
    }

    @Test
    @DisplayName("非 IDLE 不能 checkout")
    void cannotCheckoutWhenRented() {
        BatteryAsset rented =
                BatteryAsset.createIdle("BAT-1", "ORG-1", "v", "m").checkout("U-1");
        assertThrows(BatteryAsset.IllegalTransitionException.class, () -> rented.checkout("U-2"));
    }
}
