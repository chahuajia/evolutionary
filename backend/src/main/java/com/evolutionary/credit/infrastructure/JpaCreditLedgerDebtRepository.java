package com.evolutionary.credit.infrastructure;

import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.CreditLedgerDebtRepository;
import com.evolutionary.credit.domain.CreditLedgerDebt;
import com.evolutionary.credit.domain.DebtStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 信用负债 JPA 适配。 */
@Component
public final class JpaCreditLedgerDebtRepository implements CreditLedgerDebtRepository {

    private final CreditLedgerDebtJpaRepository jpa;

    public JpaCreditLedgerDebtRepository(CreditLedgerDebtJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(CreditLedgerDebt debt) {
        jpa.save(
                new CreditLedgerDebtJpaEntity(
                        debt.id(),
                        debt.userId(),
                        debt.orderId(),
                        debt.amount().cents(),
                        debt.amount().currency().name(),
                        debt.status(),
                        debt.createdAt(),
                        debt.billedStatementId(),
                        debt.paidAt()));
    }

    @Override
    public Optional<CreditLedgerDebt> findById(String id) {
        return jpa.findById(id).map(JpaCreditLedgerDebtRepository::toDomain);
    }

    @Override
    public List<CreditLedgerDebt> findByUserIdAndStatus(String userId, DebtStatus status) {
        return jpa.findByUserIdAndStatus(userId, status).stream()
                .map(JpaCreditLedgerDebtRepository::toDomain)
                .toList();
    }

    @Override
    public List<CreditLedgerDebt> findByStatus(DebtStatus status) {
        return jpa.findByStatus(status).stream()
                .map(JpaCreditLedgerDebtRepository::toDomain)
                .toList();
    }

    @Override
    public List<CreditLedgerDebt> findByBilledStatementId(String statementId) {
        return jpa.findByBilledStatementId(statementId).stream()
                .map(JpaCreditLedgerDebtRepository::toDomain)
                .toList();
    }

    @Override
    public List<CreditLedgerDebt> findByOrderId(String orderId) {
        return jpa.findByOrderId(orderId).stream()
                .map(JpaCreditLedgerDebtRepository::toDomain)
                .toList();
    }

    private static CreditLedgerDebt toDomain(CreditLedgerDebtJpaEntity row) {
        Money amount = new Money(row.getAmountCents(), Currency.valueOf(row.getAmountCurrency()));
        return CreditLedgerDebt.rehydrate(
                row.getId(),
                row.getUserId(),
                row.getOrderId(),
                amount,
                row.getStatus(),
                row.getCreatedAt(),
                row.getBilledStatementId(),
                row.getPaidAt());
    }
}
