package com.evolutionary.settlement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.settlement.domain.AccrualStatus;
import com.evolutionary.settlement.domain.ProfitShareAccrual;
import com.evolutionary.settlement.domain.ProfitSharingRule;
import com.evolutionary.settlement.domain.ProfitSplit;
import com.evolutionary.settlement.domain.ReferralBinding;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AC-32 / AC-33 / AC-38 / AC-39：ORDER_COMPLETED 记 PENDING 分润意向。 */
class AccrueOnOrderCompletedTest {

    private static final Instant T0 = Instant.parse("2026-09-17T12:00:00Z");
    private static final String L1 = "ORG-L1";
    private static final String L2 = "ORG-L2";

    private InMemoryRuleRepo rules;
    private InMemoryBindingRepo bindings;
    private InMemoryAccrualRepo accruals;
    private AccrueOnOrderCompleted accrue;

    @BeforeEach
    void setUp() {
        rules = new InMemoryRuleRepo();
        bindings = new InMemoryBindingRepo();
        accruals = new InMemoryAccrualRepo();
        accrue = new AccrueOnOrderCompleted(rules, bindings, accruals);

        rules.save(
                ProfitSharingRule.create(
                        "R1",
                        L2,
                        List.of(ProfitSplit.of(L2, 10), ProfitSplit.of(L1, 5)),
                        T0.minusSeconds(3600),
                        1));
    }

    @Test
    @DisplayName("AC-32：未发 ORDER_COMPLETED → 无 Accrual；完成后产生 L2/L1/PLATFORM")
    void onlyCompletedCreatesAccrual() {
        assertTrue(accruals.findAll().isEmpty());

        OrderCompletedFact fact = completed("O1", "U-1", 10_000);
        List<ProfitShareAccrual> created = accrue.execute(fact);

        assertEquals(3, created.size());
        assertTrue(created.stream().allMatch(a -> a.status() == AccrualStatus.PENDING));
        assertEquals(3, accruals.findByOrderId("O1").size());
    }

    @Test
    @DisplayName("AC-33：100.00 → L2=10 / L1=5 / PLATFORM=85，合计 100")
    void amountsMatchRule() {
        List<ProfitShareAccrual> created = accrue.execute(completed("O1", "U-1", 10_000));
        Map<String, Long> byOrg = amountsByOrg(created);

        assertEquals(1_000L, byOrg.get(L2));
        assertEquals(500L, byOrg.get(L1));
        assertEquals(8_500L, byOrg.get(ProfitSharingRule.PLATFORM_ORG_ID));
        assertEquals(10_000L, byOrg.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    @DisplayName("AC-38：绑定已过期 → 不产生推广补贴 Accrual")
    void expiredBindingNoPromoterBonus() {
        rules.save(
                ProfitSharingRule.create(
                        "R1",
                        L2,
                        List.of(ProfitSplit.of(L2, 10), ProfitSplit.of(L1, 5)),
                        2,
                        T0.minusSeconds(3600),
                        1));
        Instant boundAt = T0.minusSeconds(ReferralBinding.BINDING_WINDOW_SECONDS + 1);
        bindings.save(ReferralBinding.bind("U-1", L2, boundAt));

        List<ProfitShareAccrual> created = accrue.execute(completed("O1", "U-1", 10_000));
        Map<String, Long> byOrg = amountsByOrg(created);

        assertEquals(8_500L, byOrg.get(ProfitSharingRule.PLATFORM_ORG_ID));
        assertEquals(1_000L, byOrg.get(L2));
        assertEquals(3, created.size());
    }

    @Test
    @DisplayName("AC-39：有效绑定 + 补贴 2% → promoter=2，PLATFORM=83")
    void promoterBonusFromPlatform() {
        rules.save(
                ProfitSharingRule.create(
                        "R1",
                        L2,
                        List.of(ProfitSplit.of(L2, 10), ProfitSplit.of(L1, 5)),
                        2,
                        T0.minusSeconds(3600),
                        1));
        bindings.save(ReferralBinding.bind("U-1", L2, T0.minusSeconds(3600)));

        List<ProfitShareAccrual> created = accrue.execute(completed("O1", "U-1", 10_000));
        Map<String, Long> byOrg = amountsByOrg(created);

        assertEquals(8_300L, byOrg.get(ProfitSharingRule.PLATFORM_ORG_ID));
        // L2 基础 10% + 推广补贴 2%
        assertEquals(1_200L, byOrg.get(L2));
        assertEquals(500L, byOrg.get(L1));
        assertEquals(10_000L, byOrg.values().stream().mapToLong(Long::longValue).sum());
    }

    private static OrderCompletedFact completed(String orderId, String userId, long cents) {
        return new OrderCompletedFact(orderId, userId, L2, cents, "CNY", T0);
    }

    private static Map<String, Long> amountsByOrg(List<ProfitShareAccrual> list) {
        return list.stream()
                .collect(
                        Collectors.groupingBy(
                                ProfitShareAccrual::orgId,
                                Collectors.summingLong(ProfitShareAccrual::amountCents)));
    }

    private static final class InMemoryRuleRepo implements ProfitSharingRuleRepository {
        private final Map<String, ProfitSharingRule> byOrg = new HashMap<>();

        @Override
        public void save(ProfitSharingRule rule) {
            byOrg.put(rule.orgId(), rule);
        }

        @Override
        public Optional<ProfitSharingRule> findByOrgId(String orgId) {
            return Optional.ofNullable(byOrg.get(orgId));
        }
    }

    private static final class InMemoryBindingRepo implements ReferralBindingRepository {
        private final Map<String, ReferralBinding> byUser = new HashMap<>();

        @Override
        public void save(ReferralBinding binding) {
            byUser.put(binding.userId(), binding);
        }

        @Override
        public Optional<ReferralBinding> findActiveByUserId(String userId) {
            return Optional.ofNullable(byUser.get(userId));
        }
    }

    private static final class InMemoryAccrualRepo implements ProfitShareAccrualRepository {
        private final List<ProfitShareAccrual> all = new ArrayList<>();

        @Override
        public void saveAll(List<ProfitShareAccrual> items) {
            all.addAll(items);
        }

        @Override
        public List<ProfitShareAccrual> findByOrderId(String orderId) {
            return all.stream().filter(a -> a.orderId().equals(orderId)).toList();
        }

        @Override
        public List<ProfitShareAccrual> findAll() {
            return List.copyOf(all);
        }
    }
}
