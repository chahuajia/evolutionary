package com.evolutionary.iot.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.iot.application.DeviceShadowRepository;
import com.evolutionary.iot.domain.DeviceShadow;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片45a：DeviceShadow JPA 落库。 */
@SpringBootTest
class JpaDeviceShadowRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T04:00:00Z");

    @Autowired
    private DeviceShadowRepository shadows;

    @Autowired
    private DeviceShadowJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("seed → save → findByBatteryId 命中")
    void seedSaveAndFindByBatteryId() {
        shadows.save(DeviceShadow.seed("BAT-T1", "vendorA", "ext-a-1", 80, 4200, T0));

        DeviceShadow found = shadows.findByBatteryId("BAT-T1").orElseThrow();
        assertEquals("BAT-T1", found.batteryId());
        assertEquals("vendorA", found.vendorId());
        assertEquals("ext-a-1", found.externalDeviceId());
        assertEquals(80, found.soc());
        assertEquals(4200, found.voltageMilli());
        assertEquals(T0, found.lastSeenAt());
        assertTrue(jpa.findById("BAT-T1").isPresent());
    }

    @Test
    @DisplayName("get 未知 batteryId → IllegalArgumentException")
    void getUnknownBatteryIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> shadows.get("BAT-MISSING"));
    }
}
