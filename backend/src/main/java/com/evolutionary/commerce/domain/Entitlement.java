package com.evolutionary.commerce.domain;

import java.time.Instant;
import java.util.Objects;

public final class Entitlement {

    private final String id;
    private final String orderId;
    private final String userId;
    private final String productId;
    private final Instant validFrom;
    /** null = 无窗口截止（PAY_AS_YOU_GO）；TIME_WINDOW 必有值。 */
    private final Instant validUntil;
    private final EntitlementStatus status;
    /** null = UNLIMITED；非 null = FINITE 剩余次数（INV-6/7）。 */
    private final Integer remainingSwaps;
    /** null = 非计量；PAY_AS_YOU_GO = 后付计量。 */
    private final MeteringMode meteringMode;

    private Entitlement(
            String id,
            String orderId,
            String userId,
            String productId,
            Instant validFrom,
            Instant validUntil,
            EntitlementStatus status,
            Integer remainingSwaps,
            MeteringMode meteringMode) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.status = status;
        this.remainingSwaps = remainingSwaps;
        this.meteringMode = meteringMode;
    }

    /** INV-2：仅对已支付订单生成 ACTIVE 权益（TIME_WINDOW）。 */
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
        if (product.isMetered()) {
            throw new IllegalArgumentException("use createPayAsYouGo for METERED product");
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
                remaining,
                null);
    }

    /** P3：PAY_AS_YOU_GO，无 validUntil，每次 swap 后结算。 */
    public static Entitlement createPayAsYouGo(
            String id, String orderId, String userId, String productId, Instant now) {
        return new Entitlement(
                requireId(id),
                requireId(orderId),
                requireId(userId),
                requireId(productId),
                Objects.requireNonNull(now, "now"),
                null,
                EntitlementStatus.ACTIVE,
                null,
                MeteringMode.PAY_AS_YOU_GO);
    }

    public boolean isActiveAt(Instant at) {
        if (status != EntitlementStatus.ACTIVE || at.isBefore(validFrom)) {
            return false;
        }
        // PAY_AS_YOU_GO：validUntil 为 null，不因窗口误杀
        if (validUntil == null) {
            return true;
        }
        return at.isBefore(validUntil);
    }

    public boolean isFinite() {
        return remainingSwaps != null;
    }

    public boolean isPayAsYouGo() {
        return meteringMode == MeteringMode.PAY_AS_YOU_GO;
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
                remainingSwaps - 1,
                meteringMode);
    }

    /** INV-5：退款时撤销权益，阻止后续 COMPLETED 履约。 */
    public Entitlement revoke() {
        if (status == EntitlementStatus.REVOKED) {
            return this;
        }
        if (status != EntitlementStatus.ACTIVE
                && status != EntitlementStatus.EXPIRED
                && status != EntitlementStatus.FROZEN) {
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
                remainingSwaps,
                meteringMode);
    }

    /** 信用逾期：ACTIVE → FROZEN（可恢复）。 */
    public Entitlement freeze() {
        if (status == EntitlementStatus.FROZEN) {
            return this;
        }
        if (status != EntitlementStatus.ACTIVE) {
            throw new IllegalStateException("cannot freeze entitlement in status " + status);
        }
        return new Entitlement(
                id,
                orderId,
                userId,
                productId,
                validFrom,
                validUntil,
                EntitlementStatus.FROZEN,
                remainingSwaps,
                meteringMode);
    }

    /** 还款解冻：FROZEN → ACTIVE。 */
    public Entitlement unfreeze() {
        if (status != EntitlementStatus.FROZEN) {
            throw new IllegalStateException("cannot unfreeze entitlement in status " + status);
        }
        return new Entitlement(
                id,
                orderId,
                userId,
                productId,
                validFrom,
                validUntil,
                EntitlementStatus.ACTIVE,
                remainingSwaps,
                meteringMode);
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

    public MeteringMode meteringMode() {
        return meteringMode;
    }

    public static Entitlement rehydrate(
            String id,
            String orderId,
            String userId,
            String productId,
            Instant validFrom,
            Instant validUntil,
            EntitlementStatus status) {
        return rehydrate(id, orderId, userId, productId, validFrom, validUntil, status, null, null);
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
        return rehydrate(
                id, orderId, userId, productId, validFrom, validUntil, status, remainingSwaps, null);
    }

    public static Entitlement rehydrate(
            String id,
            String orderId,
            String userId,
            String productId,
            Instant validFrom,
            Instant validUntil,
            EntitlementStatus status,
            Integer remainingSwaps,
            MeteringMode meteringMode) {
        if (remainingSwaps != null && remainingSwaps < 0) {
            throw new IllegalArgumentException("remainingSwaps must not be negative");
        }
        if (meteringMode != MeteringMode.PAY_AS_YOU_GO) {
            Objects.requireNonNull(validUntil, "validUntil");
        }
        return new Entitlement(
                requireId(id),
                requireId(orderId),
                requireId(userId),
                requireId(productId),
                Objects.requireNonNull(validFrom, "validFrom"),
                validUntil,
                Objects.requireNonNull(status, "status"),
                remainingSwaps,
                meteringMode);
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }
}
