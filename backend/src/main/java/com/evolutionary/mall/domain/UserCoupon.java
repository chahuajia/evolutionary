package com.evolutionary.mall.domain;

import java.util.Objects;

/** 用户持有的优惠券实例。 */
public final class UserCoupon {

    /**
     * 用户持券状态。
     */
    public enum Status {
        AVAILABLE,
        LOCKED,
        USED,
        EXPIRED
    }


    private final String id;
    private final String userId;
    private final String templateId;
    private final UserCoupon.Status status;
    private final String lockedByOrderId;

    private UserCoupon(
            String id,
            String userId,
            String templateId,
            UserCoupon.Status status,
            String lockedByOrderId) {
        this.id = id;
        this.userId = userId;
        this.templateId = templateId;
        this.status = status;
        this.lockedByOrderId = lockedByOrderId;
    }

    /** JPA 回放；不做业务校验。 */
    public static UserCoupon rehydrate(
            String id,
            String userId,
            String templateId,
            UserCoupon.Status status,
            String lockedByOrderId) {
        return new UserCoupon(
                id,
                userId,
                templateId,
                Objects.requireNonNull(status, "status"),
                lockedByOrderId);
    }

    public static UserCoupon issue(String id, String userId, String templateId) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("userCoupon id 不能为空");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (templateId == null || templateId.isBlank()) {
            throw new IllegalArgumentException("templateId 不能为空");
        }
        return new UserCoupon(id, userId, templateId, UserCoupon.Status.AVAILABLE, null);
    }

    public MallOutcome<UserCoupon> lock(String orderId) {
        Objects.requireNonNull(orderId, "orderId");
        if (status != UserCoupon.Status.AVAILABLE) {
            return MallOutcome.err(MallErrorCode.COUPON_NOT_AVAILABLE, "券不可锁定");
        }
        return MallOutcome.ok(
                new UserCoupon(id, userId, templateId, UserCoupon.Status.LOCKED, orderId));
    }

    public MallOutcome<UserCoupon> markUsed() {
        if (status != UserCoupon.Status.LOCKED && status != UserCoupon.Status.AVAILABLE) {
            return MallOutcome.err(MallErrorCode.COUPON_NOT_AVAILABLE, "券不可核销");
        }
        return MallOutcome.ok(
                new UserCoupon(id, userId, templateId, UserCoupon.Status.USED, lockedByOrderId));
    }

    public boolean isAvailable() {
        return status == UserCoupon.Status.AVAILABLE;
    }

    public String id() {
        return id;
    }

    public String userId() {
        return userId;
    }

    public String templateId() {
        return templateId;
    }

    public UserCoupon.Status status() {
        return status;
    }

    public String lockedByOrderId() {
        return lockedByOrderId;
    }
}
