package com.evolutionary.credit.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditProfileJpaRepository extends JpaRepository<CreditProfileJpaEntity, String> {}
