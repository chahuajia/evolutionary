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

    private Entitlement(
            String id,
            String orderId,
            String userId,
            String productId,
            Instant validFrom,
            Instant validUntil,
            EntitlementStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.status = status;
    }

    /** INV-2：仅对已支付订单生成 ACTIVE 权益。 */
    public static Entitlement createActive(
            String id, Order paidOrder, Product product, Instant now) {
        Objects.requireNonNull(paidOrder, "paidOrder");
        if (!paidOrder.isPaid()) {
            throw new IllegalArgumentException("entitlement requires paid order");
        }
        if (!paidOrder.productId().equals(product.id())) {
            throw new IllegalArgumentException("product mismatch");
        }
        Instant validUntil = now.plusSeconds(product.durationDays() * 86_400L);
        return new Entitlement(
                requireId(id),
                paidOrder.id(),
                paidOrder.userId(),
                product.id(),
                now,
                validUntil,
                EntitlementStatus.ACTIVE);
    }

    public boolean isActiveAt(Instant at) {
        return status == EntitlementStatus.ACTIVE
                && !at.isBefore(validFrom)
                && at.isBefore(validUntil);
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

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }
}
