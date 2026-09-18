package com.evolutionary.commerce.domain;

import java.util.Objects;

/**
 * 换电履约用的电池台账（phase-0）。
 *
 * <p>与 {@code com.evolutionary.battery.domain.Battery}（站点库存 AVAILABLE/IN_USE）分离：
 * 本类按规格用 idle/rented，不混入旧换电 demo 状态机。
 */
public final class BatteryAsset {

    public enum Status {
        IDLE,
        RENTED,
        MAINTENANCE
    }


    private final String id;
    private final String orgId;
    private final String vendor;
    private final String model;
    private final BatteryAsset.Status status;
    private final String currentHolderId;

    private BatteryAsset(
            String id,
            String orgId,
            String vendor,
            String model,
            BatteryAsset.Status status,
            String currentHolderId) {
        this.id = id;
        this.orgId = orgId;
        this.vendor = vendor;
        this.model = model;
        this.status = status;
        this.currentHolderId = currentHolderId;
    }

    public static BatteryAsset createIdle(
            String id, String orgId, String vendor, String model) {
        return new BatteryAsset(
                requireId(id),
                requireId(orgId),
                Objects.requireNonNull(vendor, "vendor"),
                Objects.requireNonNull(model, "model"),
                BatteryAsset.Status.IDLE,
                null);
    }

    public static BatteryAsset rehydrate(
            String id,
            String orgId,
            String vendor,
            String model,
            BatteryAsset.Status status,
            String currentHolderId) {
        return new BatteryAsset(
                requireId(id),
                requireId(orgId),
                vendor,
                model,
                Objects.requireNonNull(status, "status"),
                currentHolderId);
    }

    /** idle → rented（UsageEvent STARTED）。 */
    public BatteryAsset checkout(String userId) {
        if (status != BatteryAsset.Status.IDLE) {
            throw new IllegalTransitionException(BatteryAsset.Status.RENTED);
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        return new BatteryAsset(id, orgId, vendor, model, BatteryAsset.Status.RENTED, userId);
    }

    /** rented → idle（UsageEvent COMPLETED）。INV-4 */
    public BatteryAsset returnToIdle() {
        if (status != BatteryAsset.Status.RENTED) {
            throw new IllegalTransitionException(BatteryAsset.Status.IDLE);
        }
        return new BatteryAsset(id, orgId, vendor, model, BatteryAsset.Status.IDLE, null);
    }

    public boolean isIdle() {
        return status == BatteryAsset.Status.IDLE;
    }

    public String id() {
        return id;
    }

    public String orgId() {
        return orgId;
    }

    public String vendor() {
        return vendor;
    }

    public String model() {
        return model;
    }

    public BatteryAsset.Status status() {
        return status;
    }

    public String currentHolderId() {
        return currentHolderId;
    }

    public static final class IllegalTransitionException extends RuntimeException {
        IllegalTransitionException(BatteryAsset.Status next) {
            super("illegal battery asset transition to " + next);
        }
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }
}
