package com.evolutionary.commerce.domain;

import java.time.Instant;
import java.util.Objects;

public final class Order {

    private final String id;
    private final String userId;
    private final String productId;
    private final String orgId;
    private final OrderStatus status;
    private final Money paidAmount;
    private final Instant createdAt;
    private final Instant paidAt;
    private final Instant refundedAt;

    private Order(
            String id,
            String userId,
            String productId,
            String orgId,
            OrderStatus status,
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
                OrderStatus.CREATED,
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
            OrderStatus status,
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
        if (status != OrderStatus.CREATED) {
            throw illegalTransition(OrderStatus.PAID);
        }
        Objects.requireNonNull(at, "paidAt");
        return new Order(
                id, userId, productId, orgId, OrderStatus.PAID, paidAmount, createdAt, at, refundedAt);
    }

    public Order cancel() {
        if (status != OrderStatus.CREATED) {
            throw illegalTransition(OrderStatus.CANCELLED);
        }
        return new Order(
                id, userId, productId, orgId, OrderStatus.CANCELLED, paidAmount, createdAt, paidAt, refundedAt);
    }

    public Order refund(Instant at) {
        if (status != OrderStatus.PAID) {
            throw illegalTransition(OrderStatus.REFUNDED);
        }
        Objects.requireNonNull(at, "refundedAt");
        return new Order(
                id, userId, productId, orgId, OrderStatus.REFUNDED, paidAmount, createdAt, paidAt, at);
    }

    public boolean isPaid() {
        return status == OrderStatus.PAID;
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

    public OrderStatus status() {
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
        IllegalTransitionException(OrderStatus next) {
            super("illegal order transition to " + next);
        }
    }

    private IllegalTransitionException illegalTransition(OrderStatus next) {
        return new IllegalTransitionException(next);
    }
}
