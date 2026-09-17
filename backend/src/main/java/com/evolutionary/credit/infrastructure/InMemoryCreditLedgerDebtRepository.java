package com.evolutionary.credit.infrastructure;

import com.evolutionary.credit.application.CreditLedgerDebtRepository;
import com.evolutionary.credit.domain.CreditLedgerDebt;
import com.evolutionary.credit.domain.DebtStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内信用负债仓储（还款路径最小实现）。 */
public final class InMemoryCreditLedgerDebtRepository implements CreditLedgerDebtRepository {

    private final Map<String, CreditLedgerDebt> byId = new ConcurrentHashMap<>();

    @Override
    public void save(CreditLedgerDebt debt) {
        byId.put(debt.id(), debt);
    }

    @Override
    public Optional<CreditLedgerDebt> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<CreditLedgerDebt> findByUserIdAndStatus(String userId, DebtStatus status) {
        return byId.values().stream()
                .filter(d -> d.userId().equals(userId) && d.status() == status)
                .toList();
    }

    @Override
    public List<CreditLedgerDebt> findByStatus(DebtStatus status) {
        return byId.values().stream().filter(d -> d.status() == status).toList();
    }

    @Override
    public List<CreditLedgerDebt> findByBilledStatementId(String statementId) {
        return byId.values().stream()
                .filter(d -> statementId.equals(d.billedStatementId()))
                .toList();
    }

    @Override
    public List<CreditLedgerDebt> findByOrderId(String orderId) {
        return byId.values().stream().filter(d -> d.orderId().equals(orderId)).toList();
    }
}
