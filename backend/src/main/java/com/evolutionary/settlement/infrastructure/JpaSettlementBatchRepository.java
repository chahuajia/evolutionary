package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.application.SettlementBatchRepository;
import com.evolutionary.settlement.domain.SettlementBatch;
import org.springframework.stereotype.Component;

/** 结算批 JPA 适配。 */
@Component
public final class JpaSettlementBatchRepository implements SettlementBatchRepository {

    private final SettlementBatchJpaRepository jpa;

    public JpaSettlementBatchRepository(SettlementBatchJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(SettlementBatch batch) {
        jpa.save(
                new SettlementBatchJpaEntity(
                        batch.id(),
                        batch.periodStart(),
                        batch.periodEnd(),
                        batch.status(),
                        batch.createdAt(),
                        batch.closedAt()));
    }

    @Override
    public SettlementBatch get(String batchId) {
        return jpa.findById(batchId)
                .map(JpaSettlementBatchRepository::toDomain)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "unknown settlement batch: " + batchId));
    }

    private static SettlementBatch toDomain(SettlementBatchJpaEntity row) {
        return SettlementBatch.rehydrate(
                row.getId(),
                row.getPeriodStart(),
                row.getPeriodEnd(),
                row.getStatus(),
                row.getCreatedAt(),
                row.getClosedAt());
    }
}
