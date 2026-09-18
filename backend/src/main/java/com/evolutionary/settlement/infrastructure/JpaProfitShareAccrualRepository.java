package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.application.ProfitShareAccrualRepository;
import com.evolutionary.settlement.domain.ProfitShareAccrual;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

/** 分润意向 JPA 适配。 */
@Component
public final class JpaProfitShareAccrualRepository implements ProfitShareAccrualRepository {

    private final ProfitShareAccrualJpaRepository jpa;

    public JpaProfitShareAccrualRepository(ProfitShareAccrualJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(ProfitShareAccrual accrual) {
        jpa.save(toEntity(accrual));
    }

    @Override
    public void saveAll(List<ProfitShareAccrual> accruals) {
        jpa.saveAll(accruals.stream().map(JpaProfitShareAccrualRepository::toEntity).toList());
    }

    @Override
    public List<ProfitShareAccrual> findByOrderId(String orderId) {
        return jpa.findByOrderId(orderId).stream()
                .map(JpaProfitShareAccrualRepository::toDomain)
                .toList();
    }

    @Override
    public List<ProfitShareAccrual> findPendingCreatedBetween(
            Instant periodStart, Instant periodEnd) {
        return jpa.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        ProfitShareAccrual.Status.PENDING, periodStart, periodEnd)
                .stream()
                .map(JpaProfitShareAccrualRepository::toDomain)
                .toList();
    }

    @Override
    public List<ProfitShareAccrual> findAll() {
        return jpa.findAll().stream().map(JpaProfitShareAccrualRepository::toDomain).toList();
    }

    private static ProfitShareAccrualJpaEntity toEntity(ProfitShareAccrual accrual) {
        return new ProfitShareAccrualJpaEntity(
                accrual.id(),
                accrual.orderId(),
                accrual.orgId(),
                accrual.amountCents(),
                accrual.currency(),
                accrual.ruleVersion(),
                accrual.status(),
                accrual.createdAt(),
                accrual.settledAt(),
                accrual.batchId(),
                accrual.reversalOf());
    }

    private static ProfitShareAccrual toDomain(ProfitShareAccrualJpaEntity row) {
        return ProfitShareAccrual.rehydrate(
                row.getId(),
                row.getOrderId(),
                row.getOrgId(),
                row.getAmountCents(),
                row.getCurrency(),
                row.getRuleVersion(),
                row.getStatus(),
                row.getCreatedAt(),
                row.getSettledAt(),
                row.getBatchId(),
                row.getReversalOf());
    }
}
