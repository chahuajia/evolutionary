package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.domain.BatteryAssetStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatteryAssetJpaRepository extends JpaRepository<BatteryAssetJpaEntity, String> {

    Optional<BatteryAssetJpaEntity> findFirstByStatus(BatteryAssetStatus status);
}
