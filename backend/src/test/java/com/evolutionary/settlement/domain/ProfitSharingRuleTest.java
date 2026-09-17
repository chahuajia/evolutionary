package com.evolutionary.settlement.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfitSharingRuleTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");

    @Test
    @DisplayName("百分比之和超过 100 拒绝")
    void rejectsOver100() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ProfitSharingRule.create(
                                "R1",
                                "ORG-L1",
                                List.of(ProfitSplit.of("ORG-L2", 60), ProfitSplit.of("ORG-L1", 50)),
                                T0,
                                1));
    }

    @Test
    @DisplayName("余量归 PLATFORM")
    void remainderToPlatform() {
        ProfitSharingRule rule =
                ProfitSharingRule.create(
                        "R1",
                        "ORG-L1",
                        List.of(ProfitSplit.of("ORG-L2", 10), ProfitSplit.of("ORG-L1", 5)),
                        T0,
                        1);
        List<ProfitSharingRule.AllocatedShare> shares = rule.allocate(10_000);
        assertEquals(3, shares.size());
        assertEquals(1_000, shares.get(0).amountCents());
        assertEquals(500, shares.get(1).amountCents());
        assertEquals(ProfitSharingRule.PLATFORM_ORG_ID, shares.get(2).orgId());
        assertEquals(8_500, shares.get(2).amountCents());
    }

    @Test
    @DisplayName("推广补贴从 PLATFORM 扣")
    void promoterBonusFromPlatform() {
        ProfitSharingRule rule =
                ProfitSharingRule.create(
                        "R1",
                        "ORG-L2",
                        List.of(ProfitSplit.of("ORG-L2", 10), ProfitSplit.of("ORG-L1", 5)),
                        2,
                        T0,
                        1);
        List<ProfitSharingRule.AllocatedShare> shares =
                rule.allocate(10_000, "ORG-L2", true);
        assertEquals(8_300, shares.stream()
                .filter(s -> ProfitSharingRule.PLATFORM_ORG_ID.equals(s.orgId()))
                .findFirst()
                .orElseThrow()
                .amountCents());
        long promoter =
                shares.stream()
                        .filter(s -> "ORG-L2".equals(s.orgId()))
                        .mapToLong(ProfitSharingRule.AllocatedShare::amountCents)
                        .sum();
        assertEquals(1_200, promoter);
    }
}

class ReferralBindingTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");

    @Test
    @DisplayName("72 小时窗口内有效")
    void coversWithinWindow() {
        ReferralBinding b = ReferralBinding.bind("U-1", "ORG-L2", T0);
        assertTrue(b.coversOrderAt(T0.plusSeconds(3600)));
        assertFalse(b.coversOrderAt(T0.plusSeconds(ReferralBinding.BINDING_WINDOW_SECONDS)));
    }
}
