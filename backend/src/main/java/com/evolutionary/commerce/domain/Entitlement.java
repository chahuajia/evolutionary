package com.evolutionary.commerce.domain;

import java.time.Instant;
import java.util.Objects;

public final class Entitlement {

    private final String id;
    private final String orderId;
    private final String userId;
    private final String productId;
    private final Instant validFrom;
    private final Instant validUntil;
    private final EntitlementStatus status;
    /** null = UNLIMITED；非 null = FINITE 剩余次数（INV-6/7）。 */
    private final Integer remainingSwaps;

    private Entitlement(
            String id,
            String orderId,
            String userId,
            String productId,
            Instant validFrom,
            Instant validUntil,
            EntitlementStatus status,
            Integer remainingSwaps) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.status = status;
        this.remainingSwaps = remainingSwaps;
    }

    /** INV-2：仅对已支付订单生成 ACTIVE 权益。 */
    public static Entitlement createActive(
            String id, Order paidOrder, Product product, Instant now) {
        Objects.requireNonNull(paidOrder, "paidOrder");
        Objects.requireNonNull(product, "product");
        if (!paidOrder.isPaid()) {
            throw new IllegalArgumentException("entitlement requires paid order");
        }
        if (!paidOrder.productId().equals(product.id())) {
            throw new IllegalArgumentException("product mismatch");
        }
        Instant validUntil = now.plusSeconds(product.durationDays() * 86_400L);
        Integer remaining = product.swapLimit().finiteOrNull();
        return new Entitlement(
                requireId(id),
                paidOrder.id(),
                paidOrder.userId(),
                product.id(),
                now,
                validUntil,
                EntitlementStatus.ACTIVE,
                remaining);
    }

    public boolean isActiveAt(Instant at) {
        return status == EntitlementStatus.ACTIVE
                && !at.isBefore(validFrom)
                && at.isBefore(validUntil);
    }

    public boolean isFinite() {
        return remainingSwaps != null;
    }

    public boolean isExhausted() {
        return remainingSwaps != null && remainingSwaps == 0;
    }

    /** INV-6：COMPLETED 后 remainingSwaps -= 1。 */
    public Entitlement consumeSwap() {
        if (remainingSwaps == null) {
            return this;
        }
        if (remainingSwaps <= 0) {
            throw new IllegalStateException("cannot consume exhausted entitlement");
        }
        return new Entitlement(
                id,
                orderId,
                userId,
                productId,
                validFrom,
                validUntil,
                status,
                remainingSwaps - 1);
    }

    /** INV-5：退款时撤销权益，阻止后续 COMPLETED 履约。 */
    public Entitlement revoke() {
        if (status == EntitlementStatus.REVOKED) {
            return this;
        }
        if (status != EntitlementStatus.ACTIVE && status != EntitlementStatus.EXPIRED) {
            throw new IllegalStateException("cannot revoke entitlement in status " + status);
        }
        return new Entitlement(
                id,
                orderId,
                userId,
                productId,
                validFrom,
                validUntil,
                EntitlementStatus.REVOKED,
                remainingSwaps);
    }

    public String id() {
        return id;
    }

    public String orderId() {
        return orderId;
    }

    public String userId() {
        return userId;
    }

    public String productId() {
        return productId;
    }

    public Instant validFrom() {
        return validFrom;
    }

    public Instant validUntil() {
        return validUntil;
    }

    public EntitlementStatus status() {
        return status;
    }

    public Integer remainingSwaps() {
        return remainingSwaps;
    }

    public static Entitlement rehydrate(
            String id,
            String orderId,
            String userId,
            String productId,
            Instant validFrom,
            Instant validUntil,
            EntitlementStatus status) {
        return rehydrate(id, orderId, userId, productId, validFrom, validUntil, status, null);
    }

    public static Entitlement rehydrate(
            String id,
            String orderId,
            String userId,
            String productId,
            Instant validFrom,
            Instant validUntil,
            EntitlementStatus status,
            Integer remainingSwaps) {
        if (remainingSwaps != null && remainingSwaps < 0) {
            throw new IllegalArgumentException("remainingSwaps must not be negative");
        }
        return new Entitlement(
                requireId(id),
                requireId(orderId),
                requireId(userId),
                requireId(productId),
                Objects.requireNonNull(validFrom, "validFrom"),
                Objects.requireNonNull(validUntil, "validUntil"),
                Objects.requireNonNull(status, "status"),
                remainingSwaps);
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }
}
