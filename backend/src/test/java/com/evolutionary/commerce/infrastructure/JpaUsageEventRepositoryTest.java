package com.evolutionary.commerce.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.application.UsageEventRepository;
import com.evolutionary.commerce.domain.MeterReading;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.UsageEvent;
import com.evolutionary.commerce.domain.UsageEventStatus;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片31a：UsageEvent JPA 落库。 */
@SpringBootTest
class JpaUsageEventRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T04:00:00Z");

    @Autowired
    private UsageEventRepository usages;

    @Autowired
    private UsageEventJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("save STARTED → findStartedByBattery 命中")
    void saveAndFindStarted() {
        usages.save(UsageEvent.start("UE-1", "U1", "E-1", "BAT-1", "CAB-1", T0));

        Optional<UsageEvent> found = usages.findStartedByBattery("BAT-1");
        assertTrue(found.isPresent());
        assertEquals("UE-1", found.get().id());
        assertEquals(UsageEventStatus.STARTED, found.get().status());
    }

    @Test
    @DisplayName("complete 计量字段落库并可回放")
    void completePersistsMeterAndCharge() {
        UsageEvent started = UsageEvent.start("UE-M", "U1", "E-M1", "BAT-M1", "CAB-1", T0);
        usages.save(started);
        usages.save(
                started.complete(
                        T0.plusSeconds(60), new MeterReading(80, 55), Money.cny(1250)));

        assertTrue(usages.findStartedByBattery("BAT-M1").isEmpty());

        UsageEventJpaEntity row = jpa.findById("UE-M").orElseThrow();
        assertEquals(UsageEventStatus.COMPLETED.name(), row.getStatus());
        assertEquals(80, row.getSocBefore());
        assertEquals(55, row.getSocAfter());
        assertEquals(1250L, row.getChargedAmountCents());
        assertEquals("CNY", row.getChargedCurrency());
    }
}
