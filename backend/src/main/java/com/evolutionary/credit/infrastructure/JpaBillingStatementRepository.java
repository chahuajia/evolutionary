package com.evolutionary.credit.infrastructure;

import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.BillingStatementRepository;
import com.evolutionary.credit.domain.BillingStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 周期账单 JPA 适配。 */
@Component
public final class JpaBillingStatementRepository implements BillingStatementRepository {

    private final BillingStatementJpaRepository jpa;

    public JpaBillingStatementRepository(BillingStatementJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(BillingStatement statement) {
        jpa.save(
                new BillingStatementJpaEntity(
                        statement.id(),
                        statement.userId(),
                        statement.periodStart(),
                        statement.periodEnd(),
                        statement.totalDue().cents(),
                        statement.totalDue().currency().name(),
                        statement.status(),
                        statement.dueDate(),
                        statement.createdAt(),
                        statement.paidAt()));
    }

    @Override
    public Optional<BillingStatement> findById(String id) {
        return jpa.findById(id).map(JpaBillingStatementRepository::toDomain);
    }

    @Override
    public List<BillingStatement> findByUserId(String userId) {
        return jpa.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(JpaBillingStatementRepository::toDomain)
                .toList();
    }

    private static BillingStatement toDomain(BillingStatementJpaEntity row) {
        Money totalDue =
                new Money(row.getTotalDueCents(), Currency.valueOf(row.getTotalDueCurrency()));
        return BillingStatement.rehydrate(
                row.getId(),
                row.getUserId(),
                row.getPeriodStart(),
                row.getPeriodEnd(),
                totalDue,
                row.getStatus(),
                row.getDueDate(),
                row.getCreatedAt(),
                row.getPaidAt());
    }
}
