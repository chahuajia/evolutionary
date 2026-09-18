package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.BatteryAssetRepository;
import com.evolutionary.commerce.domain.BatteryAsset;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 电池台账 JPA 适配。 */
@Component
public final class JpaBatteryAssetRepository implements BatteryAssetRepository {

    private final BatteryAssetJpaRepository jpa;

    public JpaBatteryAssetRepository(BatteryAssetJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<BatteryAsset> findAnyIdle() {
        return jpa.findFirstByStatus(BatteryAsset.Status.IDLE).map(JpaBatteryAssetRepository::toDomain);
    }

    @Override
    public BatteryAsset get(String batteryId) {
        return jpa.findById(batteryId)
                .map(JpaBatteryAssetRepository::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("unknown battery"));
    }

    @Override
    public void save(BatteryAsset battery) {
        jpa.save(
                new BatteryAssetJpaEntity(
                        battery.id(),
                        battery.orgId(),
                        battery.vendor(),
                        battery.model(),
                        battery.status(),
                        battery.currentHolderId()));
    }

    private static BatteryAsset toDomain(BatteryAssetJpaEntity row) {
        return BatteryAsset.rehydrate(
                row.getId(),
                row.getOrgId(),
                row.getVendor(),
                row.getModel(),
                row.getStatus(),
                row.getCurrentHolderId());
    }
}
