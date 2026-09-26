package com.evolutionary.commerce.domain;

import java.time.Instant;
import java.util.Objects;

public final class Order {

    public enum Status {
        CREATED,
        PAID,
        REFUNDED,
        CANCELLED
    }


    private final String id;
    private final String userId;
    private final String productId;
    private final String orgId;
    private final Order.Status status;
    private final Money paidAmount;
    private final Instant createdAt;
    private final Instant paidAt;
    private final Instant refundedAt;

    private Order(
            String id,
            String userId,
            String productId,
            String orgId,
            Order.Status status,
            Money paidAmount,
            Instant createdAt,
            Instant paidAt,
            Instant refundedAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.orgId = orgId;
        this.status = status;
        this.paidAmount = paidAmount;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
        this.refundedAt = refundedAt;
    }

    public static Order create(
            String id,
            String userId,
            String productId,
            String orgId,
            Money paidAmount,
            Instant createdAt) {
        return new Order(
                requireId(id),
                requireId(userId),
                requireId(productId),
                requireId(orgId),
                Order.Status.CREATED,
                Objects.requireNonNull(paidAmount, "paidAmount"),
                Objects.requireNonNull(createdAt, "createdAt"),
                null,
                null);
    }

    public static Order rehydrate(
            String id,
            String userId,
            String productId,
            String orgId,
            Order.Status status,
            Money paidAmount,
            Instant createdAt,
            Instant paidAt,
            Instant refundedAt) {
        return new Order(
                requireId(id),
                requireId(userId),
                requireId(productId),
                requireId(orgId),
                Objects.requireNonNull(status, "status"),
                Objects.requireNonNull(paidAmount, "paidAmount"),
                Objects.requireNonNull(createdAt, "createdAt"),
                paidAt,
                refundedAt);
    }

    public Order pay(Instant at) {
        if (status != Order.Status.CREATED) {
            throw illegalTransition(Order.Status.PAID);
        }
        Objects.requireNonNull(at, "paidAt");
        return new Order(
                id, userId, productId, orgId, Order.Status.PAID, paidAmount, createdAt, at, refundedAt);
    }

    public Order cancel() {
        if (status != Order.Status.CREATED) {
            throw illegalTransition(Order.Status.CANCELLED);
        }
        return new Order(
                id, userId, productId, orgId, Order.Status.CANCELLED, paidAmount, createdAt, paidAt, refundedAt);
    }

    public Order refund(Instant at) {
        if (status != Order.Status.PAID) {
            throw illegalTransition(Order.Status.REFUNDED);
        }
        Objects.requireNonNull(at, "refundedAt");
        return new Order(
                id, userId, productId, orgId, Order.Status.REFUNDED, paidAmount, createdAt, paidAt, at);
    }

    public boolean isPaid() {
        return status == Order.Status.PAID;
    }

    public String id() {
        return id;
    }

    public String userId() {
        return userId;
    }

    public String productId() {
        return productId;
    }

    public String orgId() {
        return orgId;
    }

    public Order.Status status() {
        return status;
    }

    public Money paidAmount() {
        return paidAmount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant paidAt() {
        return paidAt;
    }

    public Instant refundedAt() {
        return refundedAt;
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }

    public static final class IllegalTransitionException extends RuntimeException {
        IllegalTransitionException(Order.Status next) {
            super("illegal order transition to " + next);
        }
    }

    private IllegalTransitionException illegalTransition(Order.Status next) {
        return new IllegalTransitionException(next);
    }
}
