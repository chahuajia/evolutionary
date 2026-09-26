package com.evolutionary.iot.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** 设备影子（业务只读；INV-18）。 */
public final class DeviceShadow {

    public static final Duration STALE_AFTER = Duration.ofMinutes(5);

    private final String batteryId;
    private final String vendorId;
    private final String externalDeviceId;
    private final int soc;
    private final long voltageMilli;
    private final LockState lockState;
    private final ShadowStatus status;
    private final Instant lastSeenAt;
    private final boolean stale;
    private final Instant updatedAt;

    private DeviceShadow(
            String batteryId,
            String vendorId,
            String externalDeviceId,
            int soc,
            long voltageMilli,
            LockState lockState,
            ShadowStatus status,
            Instant lastSeenAt,
            boolean stale,
            Instant updatedAt) {
        this.batteryId = batteryId;
        this.vendorId = vendorId;
        this.externalDeviceId = externalDeviceId;
        this.soc = soc;
        this.voltageMilli = voltageMilli;
        this.lockState = lockState;
        this.status = status;
        this.lastSeenAt = lastSeenAt;
        this.stale = stale;
        this.updatedAt = updatedAt;
    }

    /** 持久化回放（infrastructure → domain）。 */
    public static DeviceShadow rehydrate(
            String batteryId,
            String vendorId,
            String externalDeviceId,
            int soc,
            long voltageMilli,
            Instant lastSeenAt) {
        if (batteryId == null || batteryId.isBlank()) {
            throw new IllegalArgumentException("batteryId 不能为空");
        }
        Instant seen = Objects.requireNonNull(lastSeenAt, "lastSeenAt");
        return new DeviceShadow(
                batteryId,
                Objects.requireNonNull(vendorId, "vendorId"),
                Objects.requireNonNull(externalDeviceId, "externalDeviceId"),
                soc,
                voltageMilli,
                LockState.UNLOCKED,
                ShadowStatus.IDLE,
                seen,
                false,
                seen);
    }

    public static DeviceShadow seed(
            String batteryId,
            String vendorId,
            String externalDeviceId,
            int soc,
            long voltageMilli,
            Instant at) {
        return new DeviceShadow(
                Objects.requireNonNull(batteryId, "batteryId"),
                Objects.requireNonNull(vendorId, "vendorId"),
                Objects.requireNonNull(externalDeviceId, "externalDeviceId"),
                soc,
                voltageMilli,
                LockState.UNLOCKED,
                ShadowStatus.IDLE,
                Objects.requireNonNull(at, "at"),
                false,
                at);
    }

    /** 应用遥测：刷新 soc/电压/lastSeenAt，清除 stale。 */
    public DeviceShadow applyTelemetry(BatteryTelemetryReported event, Instant now) {
        Objects.requireNonNull(event, "event");
        if (!batteryId.equals(event.batteryId())) {
            throw new IllegalArgumentException("batteryId 不匹配");
        }
        return new DeviceShadow(
                batteryId,
                vendorId,
                externalDeviceId,
                event.soc(),
                event.voltageMilli(),
                lockState,
                status,
                event.reportedAt(),
                false,
                Objects.requireNonNull(now, "now"));
    }

    /** 按 lastSeenAt 与阈值刷新 stale 标记。 */
    public DeviceShadow refreshStale(Instant now) {
        boolean next = Duration.between(lastSeenAt, now).compareTo(STALE_AFTER) > 0;
        if (next == stale) {
            return this;
        }
        return new DeviceShadow(
                batteryId,
                vendorId,
                externalDeviceId,
                soc,
                voltageMilli,
                lockState,
                status,
                lastSeenAt,
                next,
                Objects.requireNonNull(now, "now"));
    }

    public String batteryId() {
        return batteryId;
    }

    public String vendorId() {
        return vendorId;
    }

    public String externalDeviceId() {
        return externalDeviceId;
    }

    public int soc() {
        return soc;
    }

    public long voltageMilli() {
        return voltageMilli;
    }

    public LockState lockState() {
        return lockState;
    }

    public ShadowStatus status() {
        return status;
    }

    public Instant lastSeenAt() {
        return lastSeenAt;
    }

    public boolean stale() {
        return stale;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public enum LockState {
        LOCKED,
        UNLOCKED
    }

    public enum ShadowStatus {
        IDLE,
        RENTED,
        MAINTENANCE
    }
}
