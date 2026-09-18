package com.evolutionary.credit.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.CreditPolicyRepository;
import com.evolutionary.credit.domain.CreditPolicy;
import com.evolutionary.credit.domain.ScoreTier;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片38b：CreditPolicy JPA 落库。 */
@SpringBootTest
class JpaCreditPolicyRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T00:00:00Z");

    @Autowired
    private CreditPolicyRepository policies;

    @Autowired
    private CreditPolicyJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("save → findByVersion；各档 limitFor 命中")
    void saveAndFindByVersion() {
        Map<ScoreTier, Money> limits = new EnumMap<>(ScoreTier.class);
        limits.put(ScoreTier.A, Money.cny(50_000));
        limits.put(ScoreTier.B, Money.cny(20_000));
        limits.put(ScoreTier.C, Money.cny(5_000));
        policies.save(CreditPolicy.of("POL-JPA-1", 3, limits, T0));

        CreditPolicy found = policies.getByVersion(3);
        assertEquals("POL-JPA-1", found.id());
        assertEquals(3, found.version());
        assertEquals(50_000, found.limitFor(ScoreTier.A).cents());
        assertEquals(20_000, found.limitFor(ScoreTier.B).cents());
        assertEquals(5_000, found.limitFor(ScoreTier.C).cents());
        assertEquals(T0, found.effectiveFrom());
        assertTrue(jpa.findByVersion(3).isPresent());
    }
}
