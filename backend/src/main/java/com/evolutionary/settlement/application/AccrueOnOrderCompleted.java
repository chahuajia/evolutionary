package com.evolutionary.settlement.application;

import com.evolutionary.settlement.domain.ProfitShareAccrual;
import com.evolutionary.settlement.domain.ProfitSharingRule;
import com.evolutionary.settlement.domain.ProfitSharingRule.AllocatedShare;
import com.evolutionary.settlement.domain.ReferralBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 订单完成后记 PENDING 分润意向（P4-1 / P4-2）。
 *
 * <p>仅响应 {@link OrderCompletedFact}；PAID 未完成时不调用本用例 → 无 Accrual（AC-32）。
 */
public final class AccrueOnOrderCompleted {

    private final ProfitSharingRuleRepository rules;
    private final ReferralBindingRepository bindings;
    private final ProfitShareAccrualRepository accruals;

    public AccrueOnOrderCompleted(
            ProfitSharingRuleRepository rules,
            ReferralBindingRepository bindings,
            ProfitShareAccrualRepository accruals) {
        this.rules = Objects.requireNonNull(rules, "rules");
        this.bindings = Objects.requireNonNull(bindings, "bindings");
        this.accruals = Objects.requireNonNull(accruals, "accruals");
    }

    /**
     * @return 新建的 PENDING Accrual；若该订单已记过意向则返回已有记录（幂等）
     */
    public List<ProfitShareAccrual> execute(OrderCompletedFact fact) {
        Objects.requireNonNull(fact, "fact");

        List<ProfitShareAccrual> existing = accruals.findByOrderId(fact.orderId());
        if (!existing.isEmpty()) {
            return List.copyOf(existing);
        }

        ProfitSharingRule rule =
                rules.findByOrgId(fact.sellerOrgId())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "无售卖方分润规则: " + fact.sellerOrgId()));

        Optional<ReferralBinding> binding = bindings.findActiveByUserId(fact.userId());
        boolean applyBonus =
                binding.isPresent() && binding.get().coversOrderAt(fact.completedAt());
        String promoterOrgId = applyBonus ? binding.get().promoterOrgId() : null;

        List<AllocatedShare> shares =
                rule.allocate(fact.paidAmountCents(), promoterOrgId, applyBonus);

        List<ProfitShareAccrual> created = new ArrayList<>();
        for (AllocatedShare share : shares) {
            if (share.amountCents() <= 0) {
                continue;
            }
            created.add(
                    ProfitShareAccrual.pending(
                            UUID.randomUUID().toString(),
                            fact.orderId(),
                            share.orgId(),
                            share.amountCents(),
                            fact.currency(),
                            rule.version(),
                            fact.completedAt()));
        }
        accruals.saveAll(created);
        return List.copyOf(created);
    }
}
