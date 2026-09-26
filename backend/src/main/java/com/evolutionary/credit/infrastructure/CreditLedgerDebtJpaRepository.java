package com.evolutionary.credit.infrastructure;


import com.evolutionary.credit.domain.CreditLedgerDebt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditLedgerDebtJpaRepository
        extends JpaRepository<CreditLedgerDebtJpaEntity, String> {

    List<CreditLedgerDebtJpaEntity> findByUserIdAndStatus(String userId, CreditLedgerDebt.Status status);

    List<CreditLedgerDebtJpaEntity> findByStatus(CreditLedgerDebt.Status status);

    List<CreditLedgerDebtJpaEntity> findByBilledStatementId(String billedStatementId);

    List<CreditLedgerDebtJpaEntity> findByOrderId(String orderId);
}
