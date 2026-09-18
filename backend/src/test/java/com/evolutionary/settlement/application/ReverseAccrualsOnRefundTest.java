package com.evolutionary.settlement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.settlement.domain.ProfitShareAccrual;
import com.evolutionary.settlement.domain.SettlementErrorCode;
import com.evolutionary.settlement.domain.SettlementException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AC-35 / AC-36：结算前 REVERSED；已 SETTLED 不可退。 */
class ReverseAccrualsOnRefundTest {

    private static final Instant T0 = Instant.parse("2026-09-17T12:00:00Z");
    private static final Instant T1 = T0.plusSeconds(3600);

    private InMemoryAccrualRepo accruals;
    private ReverseAccrualsOnRefund reverse;

    @BeforeEach
    void setUp() {
        accruals = new InMemoryAccrualRepo();
        reverse = new ReverseAccrualsOnRefund(accruals);
    }

    @Test
    @DisplayName("AC-35：PENDING 退款 → REVERSED + reversal 记录；Batch 查不到 PENDING")
    void pendingRefundReverses() {
        seedPending("O1", Map.of("ORG-L2", 1_000L, "ORG-L1", 500L, "PLATFORM", 8_500L));

        List<ProfitShareAccrual> written =
                reverse.execute(new OrderRefundedFact("O1", T1));

        assertEquals(6, written.size()); // 3 冲销 + 3 reversal 行
        List<ProfitShareAccrual> forOrder = accruals.findByOrderId("O1");
        assertTrue(forOrder.stream().allMatch(a -> a.status() == ProfitShareAccrual.Status.REVERSED));
        assertEquals(
                3,
                forOrder.stream().filter(a -> a.reversalOf() != null).count());
        assertTrue(
                accruals
                        .findPendingCreatedBetween(T0, T0.plusSeconds(7L * 24 * 3600))
                        .isEmpty());
    }

    @Test
    @DisplayName("AC-36：已 SETTLED → ORDER_NOT_REFUNDABLE_SETTLED")
    void settledRejectsRefund() {
        ProfitShareAccrual pending =
                ProfitShareAccrual.pending("a1", "O1", "ORG-L2", 1_000L, "CNY", 1, T0);
        accruals.save(pending.settle("batch-1", T1));

        SettlementException ex =
                assertThrows(
                        SettlementException.class,
                        () -> reverse.execute(new OrderRefundedFact("O1", T1.plusSeconds(1))));
        assertEquals(SettlementErrorCode.ORDER_NOT_REFUNDABLE_SETTLED, ex.code());
    }

    private void seedPending(String orderId, Map<String, Long> amounts) {
        amounts.forEach(
                (org, cents) ->
                        accruals.save(
                                ProfitShareAccrual.pending(
                                        orderId + "-" + org, orderId, org, cents, "CNY", 1, T0)));
    }

    private static final class InMemoryAccrualRepo implements ProfitShareAccrualRepository {
        private final Map<String, ProfitShareAccrual> byId = new HashMap<>();

        @Override
        public void save(ProfitShareAccrual accrual) {
            byId.put(accrual.id(), accrual);
        }

        @Override
        public void saveAll(List<ProfitShareAccrual> items) {
            items.forEach(this::save);
        }

        @Override
        public List<ProfitShareAccrual> findByOrderId(String orderId) {
            return byId.values().stream().filter(a -> a.orderId().equals(orderId)).toList();
        }

        @Override
        public List<ProfitShareAccrual> findPendingCreatedBetween(
                Instant periodStart, Instant periodEnd) {
            return byId.values().stream()
                    .filter(a -> a.status() == ProfitShareAccrual.Status.PENDING)
                    .filter(
                            a ->
                                    !a.createdAt().isBefore(periodStart)
                                            && a.createdAt().isBefore(periodEnd))
                    .toList();
        }

        @Override
        public List<ProfitShareAccrual> findAll() {
            return List.copyOf(byId.values());
        }
    }
}
