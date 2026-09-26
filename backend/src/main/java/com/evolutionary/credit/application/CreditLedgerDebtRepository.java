package com.evolutionary.credit.application;

import com.evolutionary.credit.domain.CreditLedgerDebt;
import java.util.List;
import java.util.Optional;

public interface CreditLedgerDebtRepository {
    void save(CreditLedgerDebt debt);

    Optional<CreditLedgerDebt> findById(String id);

    List<CreditLedgerDebt> findByUserIdAndStatus(String userId, CreditLedgerDebt.Status status);

    List<CreditLedgerDebt> findByStatus(CreditLedgerDebt.Status status);

    List<CreditLedgerDebt> findByBilledStatementId(String statementId);

    List<CreditLedgerDebt> findByOrderId(String orderId);

    default CreditLedgerDebt get(String id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("未知负债: " + id));
    }
}
