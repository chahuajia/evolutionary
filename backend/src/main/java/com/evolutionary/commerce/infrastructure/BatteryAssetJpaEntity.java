package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.domain.BatteryAssetStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "battery_assets")
public class BatteryAssetJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String orgId;

    @Column(nullable = false)
    private String vendor;

    @Column(nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatteryAssetStatus status;

    private String currentHolderId;

    protected BatteryAssetJpaEntity() {}

    public BatteryAssetJpaEntity(
            String id,
            String orgId,
            String vendor,
            String model,
            BatteryAssetStatus status,
            String currentHolderId) {
        this.id = id;
        this.orgId = orgId;
        this.vendor = vendor;
        this.model = model;
        this.status = status;
        this.currentHolderId = currentHolderId;
    }

    public String getId() {
        return id;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getVendor() {
        return vendor;
    }

    public String getModel() {
        return model;
    }

    public BatteryAssetStatus getStatus() {
        return status;
    }

    public String getCurrentHolderId() {
        return currentHolderId;
    }
}
