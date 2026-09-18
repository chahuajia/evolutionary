package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.domain.UsageEventStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageEventJpaRepository extends JpaRepository<UsageEventJpaEntity, String> {

    Optional<UsageEventJpaEntity> findFirstByBatteryIdAndStatus(
            String batteryId, UsageEventStatus status);

    List<UsageEventJpaEntity> findByUserIdAndStatus(String userId, UsageEventStatus status);

    Optional<UsageEventJpaEntity> findFirstByEntitlementIdAndStatus(
            String entitlementId, UsageEventStatus status);
}
