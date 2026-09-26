package com.evolutionary.commerce.infrastructure;


import com.evolutionary.commerce.domain.UsageEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageEventJpaRepository extends JpaRepository<UsageEventJpaEntity, String> {

    Optional<UsageEventJpaEntity> findFirstByBatteryIdAndStatus(
            String batteryId, UsageEvent.Status status);

    List<UsageEventJpaEntity> findByUserIdAndStatus(String userId, UsageEvent.Status status);

    Optional<UsageEventJpaEntity> findFirstByEntitlementIdAndStatus(
            String entitlementId, UsageEvent.Status status);
}
