package com.evolutionary.credit.application;

import com.evolutionary.credit.domain.BillingStatement;
import java.util.List;
import java.util.Optional;

public interface BillingStatementRepository {
    void save(BillingStatement statement);

    Optional<BillingStatement> findById(String id);

    List<BillingStatement> findByUserId(String userId);

    default BillingStatement get(String id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("未知账单: " + id));
    }
}
