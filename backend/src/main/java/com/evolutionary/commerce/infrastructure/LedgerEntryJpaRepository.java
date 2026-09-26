package com.evolutionary.commerce.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryJpaEntity, String> {

    List<LedgerEntryJpaEntity> findByRefIdOrderByCreatedAtAsc(String refId);
}
