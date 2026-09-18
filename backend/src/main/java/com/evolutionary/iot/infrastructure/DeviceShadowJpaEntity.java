package com.evolutionary.iot.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 设备影子持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "device_shadows")
public class DeviceShadowJpaEntity {

    @Id
    private String batteryId;

    @Column(nullable = false)
    private String vendorId;

    @Column(nullable = false)
    private String externalId;

    @Column(nullable = false)
    private int soc;

    @Column(nullable = false)
    private long voltageMv;

    @Column(nullable = false)
    private Instant lastSeen;

    protected DeviceShadowJpaEntity() {}

    public DeviceShadowJpaEntity(
            String batteryId,
            String vendorId,
            String externalId,
            int soc,
            long voltageMv,
            Instant lastSeen) {
        this.batteryId = batteryId;
        this.vendorId = vendorId;
        this.externalId = externalId;
        this.soc = soc;
        this.voltageMv = voltageMv;
        this.lastSeen = lastSeen;
    }

    public String getBatteryId() {
        return batteryId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public String getExternalId() {
        return externalId;
    }

    public int getSoc() {
        return soc;
    }

    public long getVoltageMv() {
        return voltageMv;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }
}
