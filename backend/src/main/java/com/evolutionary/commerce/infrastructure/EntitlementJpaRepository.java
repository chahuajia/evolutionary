package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.domain.EntitlementStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntitlementJpaRepository extends JpaRepository<EntitlementJpaEntity, String> {

    Optional<EntitlementJpaEntity> findByOrderId(String orderId);

    List<EntitlementJpaEntity> findByUserIdAndStatus(String userId, EntitlementStatus status);
}
