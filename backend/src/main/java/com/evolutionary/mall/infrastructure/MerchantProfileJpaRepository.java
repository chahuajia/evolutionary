package com.evolutionary.mall.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantProfileJpaRepository
        extends JpaRepository<MerchantProfileJpaEntity, String> {

    Optional<MerchantProfileJpaEntity> findByOrgId(String orgId);
}
