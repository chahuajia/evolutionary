package com.evolutionary.settlement.application;

import com.evolutionary.settlement.domain.AccrualStatus;
import com.evolutionary.settlement.domain.ProfitShareAccrual;
import com.evolutionary.settlement.domain.SettlementErrorCode;
import com.evolutionary.settlement.domain.SettlementException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 结算前退款：PENDING Accrual → REVERSED，并追加 reversal 审计行（P4-3 / AC-35）。
 *
 * <p>已 SETTLED → {@link SettlementErrorCode#ORDER_NOT_REFUNDABLE_SETTLED}（AC-36）。
 */
public final class ReverseAccrualsOnRefund {

    private final ProfitShareAccrualRepository accruals;

    public ReverseAccrualsOnRefund(ProfitShareAccrualRepository accruals) {
        this.accruals = Objects.requireNonNull(accruals, "accruals");
    }

    /**
     * @return 被冲销后的原行 + 新建的 reversal 记录
     */
    public List<ProfitShareAccrual> execute(OrderRefundedFact fact) {
        Objects.requireNonNull(fact, "fact");

        List<ProfitShareAccrual> existing = accruals.findByOrderId(fact.orderId());
        if (existing.isEmpty()) {
            return List.of();
        }

        boolean anySettled =
                existing.stream().anyMatch(a -> a.status() == AccrualStatus.SETTLED);
        if (anySettled) {
            throw new SettlementException(
                    SettlementErrorCode.ORDER_NOT_REFUNDABLE_SETTLED, "已结算分润不可退款");
        }

        List<ProfitShareAccrual> pending =
                existing.stream()
                        .filter(a -> a.status() == AccrualStatus.PENDING)
                        .toList();
        if (pending.isEmpty()) {
            return List.copyOf(existing);
        }

        List<ProfitShareAccrual> written = new ArrayList<>();
        for (ProfitShareAccrual a : pending) {
            ProfitShareAccrual reversed = a.reverse();
            accruals.save(reversed);
            written.add(reversed);

            ProfitShareAccrual record =
                    ProfitShareAccrual.reversalRecord(
                            UUID.randomUUID().toString(), a, fact.refundedAt());
            accruals.save(record);
            written.add(record);
        }
        return List.copyOf(written);
    }
}
