package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.domain.UsageEventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 用量事件持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "usage_events")
public class UsageEventJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String entitlementId;

    @Column(nullable = false)
    private String batteryId;

    @Column(nullable = false)
    private String cabinetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsageEventStatus status;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant completedAt;

    private Integer meterSocBefore;

    private Integer meterSocAfter;

    private Long chargedCents;

    private String chargedCurrency;

    protected UsageEventJpaEntity() {}

    public UsageEventJpaEntity(
            String id,
            String userId,
            String entitlementId,
            String batteryId,
            String cabinetId,
            UsageEventStatus status,
            Instant startedAt,
            Instant completedAt,
            Integer meterSocBefore,
            Integer meterSocAfter,
            Long chargedCents,
            String chargedCurrency) {
        this.id = id;
        this.userId = userId;
        this.entitlementId = entitlementId;
        this.batteryId = batteryId;
        this.cabinetId = cabinetId;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.meterSocBefore = meterSocBefore;
        this.meterSocAfter = meterSocAfter;
        this.chargedCents = chargedCents;
        this.chargedCurrency = chargedCurrency;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getEntitlementId() {
        return entitlementId;
    }

    public String getBatteryId() {
        return batteryId;
    }

    public String getCabinetId() {
        return cabinetId;
    }

    public UsageEventStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Integer getMeterSocBefore() {
        return meterSocBefore;
    }

    public Integer getMeterSocAfter() {
        return meterSocAfter;
    }

    public Long getChargedCents() {
        return chargedCents;
    }

    public String getChargedCurrency() {
        return chargedCurrency;
    }
}
