package com.evolutionary.commerce.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.application.BatteryAssetRepository;
import com.evolutionary.commerce.domain.BatteryAsset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片37a：BatteryAsset JPA 落库。 */
@SpringBootTest
class JpaBatteryAssetRepositoryTest {

    @Autowired
    private BatteryAssetRepository batteries;

    @Autowired
    private BatteryAssetJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("save IDLE → get 命中")
    void saveAndGetIdle() {
        batteries.save(BatteryAsset.createIdle("BAT-JPA-1", "ORG-JPA", "vendor", "model"));

        BatteryAsset found = batteries.get("BAT-JPA-1");
        assertEquals("BAT-JPA-1", found.id());
        assertEquals("ORG-JPA", found.orgId());
        assertEquals(BatteryAsset.Status.IDLE, found.status());
        assertTrue(jpa.findById("BAT-JPA-1").isPresent());
    }

    @Test
    @DisplayName("checkout 后 status/holder 落库；findAnyIdle 命中 idle")
    void checkoutAndFindIdle() {
        batteries.save(BatteryAsset.createIdle("BAT-IDLE", "ORG-JPA", "vendor", "model"));
        batteries.save(
                BatteryAsset.createIdle("BAT-RENT", "ORG-JPA", "vendor", "model").checkout("U1"));

        BatteryAsset rented = batteries.get("BAT-RENT");
        assertEquals(BatteryAsset.Status.RENTED, rented.status());
        assertEquals("U1", rented.currentHolderId());

        assertTrue(batteries.findAnyIdle().isPresent());
        assertEquals("BAT-IDLE", batteries.findAnyIdle().orElseThrow().id());
    }
}
