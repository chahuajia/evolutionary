package com.evolutionary.commerce.domain;

import java.time.Instant;
import java.util.Objects;

public final class UsageEvent {

    private final String id;
    private final String userId;
    private final String entitlementId;
    private final String batteryId;
    private final String cabinetId;
    private final UsageEventStatus status;
    private final Instant startedAt;
    private final Instant completedAt;

    private UsageEvent(
            String id,
            String userId,
            String entitlementId,
            String batteryId,
            String cabinetId,
            UsageEventStatus status,
            Instant startedAt,
            Instant completedAt) {
        this.id = id;
        this.userId = userId;
        this.entitlementId = entitlementId;
        this.batteryId = batteryId;
        this.cabinetId = cabinetId;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static UsageEvent start(
            String id,
            String userId,
            String entitlementId,
            String batteryId,
            String cabinetId,
            Instant startedAt) {
        return new UsageEvent(
                requireId(id),
                requireId(userId),
                requireId(entitlementId),
                requireId(batteryId),
                requireId(cabinetId),
                UsageEventStatus.STARTED,
                Objects.requireNonNull(startedAt, "startedAt"),
                null);
    }

    public UsageEvent complete(Instant at) {
        if (status != UsageEventStatus.STARTED) {
            throw new IllegalTransitionException(UsageEventStatus.COMPLETED);
        }
        Objects.requireNonNull(at, "completedAt");
        return new UsageEvent(
                id, userId, entitlementId, batteryId, cabinetId, UsageEventStatus.COMPLETED, startedAt, at);
    }

    public UsageEvent fail(Instant at) {
        if (status != UsageEventStatus.STARTED) {
            throw new IllegalTransitionException(UsageEventStatus.FAILED);
        }
        Objects.requireNonNull(at, "failedAt");
        return new UsageEvent(
                id, userId, entitlementId, batteryId, cabinetId, UsageEventStatus.FAILED, startedAt, at);
    }

    public boolean isStarted() {
        return status == UsageEventStatus.STARTED;
    }

    public String id() {
        return id;
    }

    public String userId() {
        return userId;
    }

    public String entitlementId() {
        return entitlementId;
    }

    public String batteryId() {
        return batteryId;
    }

    public String cabinetId() {
        return cabinetId;
    }

    public UsageEventStatus status() {
        return status;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    public static final class IllegalTransitionException extends RuntimeException {
        IllegalTransitionException(UsageEventStatus next) {
            super("illegal usage event transition to " + next);
        }
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }
}
