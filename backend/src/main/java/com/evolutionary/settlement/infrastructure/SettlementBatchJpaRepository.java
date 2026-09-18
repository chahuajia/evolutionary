package com.evolutionary.settlement.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementBatchJpaRepository
        extends JpaRepository<SettlementBatchJpaEntity, String> {}
