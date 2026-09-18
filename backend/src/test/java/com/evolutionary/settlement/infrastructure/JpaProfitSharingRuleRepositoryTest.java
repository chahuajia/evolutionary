package com.evolutionary.settlement.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.settlement.application.ProfitSharingRuleRepository;
import com.evolutionary.settlement.domain.ProfitSharingRule;
import com.evolutionary.settlement.domain.ProfitSplit;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片43a：ProfitSharingRule JPA 落库 profit_sharing_rules。 */
@SpringBootTest
class JpaProfitSharingRuleRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-09-18T00:00:00Z");

    @Autowired
    private ProfitSharingRuleRepository rules;

    @Autowired
    private ProfitSharingRuleJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("create → save → findByOrgId")
    void saveAndFindByOrgId() {
        ProfitSharingRule created =
                ProfitSharingRule.create(
                        "R-JPA-1",
                        "ORG-L2",
                        List.of(ProfitSplit.of("ORG-L2", 10), ProfitSplit.of("ORG-L1", 5)),
                        T0,
                        1);
        rules.save(created);

        ProfitSharingRule found = rules.findByOrgId("ORG-L2").orElseThrow();
        assertEquals("R-JPA-1", found.id());
        assertEquals("ORG-L2", found.orgId());
        assertEquals(2, found.splits().size());
        assertEquals(10, found.splits().get(0).percentage());
        assertEquals(5, found.splits().get(1).percentage());
        assertEquals(0, found.promoterBonusPercent());
        assertEquals(T0, found.effectiveFrom());
        assertEquals(1, found.version());
        assertTrue(jpa.findByOrgId("ORG-L2").isPresent());
    }

    @Test
    @DisplayName("promoterBonusPercent 落库回放")
    void saveWithPromoterBonus() {
        ProfitSharingRule created =
                ProfitSharingRule.create(
                        "R-JPA-2",
                        "ORG-L2",
                        List.of(ProfitSplit.of("ORG-L2", 10), ProfitSplit.of("ORG-L1", 5)),
                        2,
                        T0,
                        1);
        rules.save(created);

        ProfitSharingRule found = rules.findByOrgId("ORG-L2").orElseThrow();
        assertEquals(2, found.promoterBonusPercent());
        assertEquals(8_300L, found.allocate(10_000, "ORG-L2", true).stream()
                .filter(s -> ProfitSharingRule.PLATFORM_ORG_ID.equals(s.orgId()))
                .findFirst()
                .orElseThrow()
                .amountCents());
    }
}
