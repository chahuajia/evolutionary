package com.evolutionary.mall.domain;


import com.evolutionary.commerce.domain.Order;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.PaymentIntent;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 商城订单聚合（独立于换电 Order）。
 *
 * <p><b>INV-16</b>：PAID 后禁止产生 Entitlement / UsageEvent——履约走物流/自提，不走换电权益。
 */
public final class MallOrder {

    /**
     * 商城订单状态（与换电 Order 独立）。
     */
    public enum Status {
        CREATED,
        PAID,
        SHIPPED,
        COMPLETED,
        REFUNDED
    }


    private final String id;
    private final String userId;
    private final String merchantOrgId;
    private final List<MallOrderLine> lines;
    private final MallOrder.Status status;
    private final PaymentIntent paymentIntent;
    private final Money discountTotal;
    private final Money paidAmount;
    private final Instant createdAt;
    private final Instant paidAt;

    private MallOrder(
            String id,
            String userId,
            String merchantOrgId,
            List<MallOrderLine> lines,
            MallOrder.Status status,
            PaymentIntent paymentIntent,
            Money discountTotal,
            Money paidAmount,
            Instant createdAt,
            Instant paidAt) {
        this.id = id;
        this.userId = userId;
        this.merchantOrgId = merchantOrgId;
        this.lines = List.copyOf(lines);
        this.status = status;
        this.paymentIntent = paymentIntent;
        this.discountTotal = discountTotal;
        this.paidAmount = paidAmount;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public static MallOrder create(
            String id,
            String userId,
            String merchantOrgId,
            List<MallOrderLine> lines,
            Money paidAmount,
            Instant createdAt) {
        return create(id, userId, merchantOrgId, lines, null, null, paidAmount, createdAt);
    }

    public static MallOrder create(
            String id,
            String userId,
            String merchantOrgId,
            List<MallOrderLine> lines,
            PaymentIntent paymentIntent,
            Money discountTotal,
            Money paidAmount,
            Instant createdAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("order id 不能为空");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (merchantOrgId == null || merchantOrgId.isBlank()) {
            throw new IllegalArgumentException("merchantOrgId 不能为空");
        }
        Objects.requireNonNull(lines, "lines");
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines 不能为空");
        }
        return new MallOrder(
                id,
                userId,
                merchantOrgId,
                lines,
                MallOrder.Status.CREATED,
                paymentIntent,
                discountTotal,
                Objects.requireNonNull(paidAmount, "paidAmount"),
                Objects.requireNonNull(createdAt, "createdAt"),
                null);
    }

    /** 从持久化层重建聚合（跳过 create 不变量）。 */
    public static MallOrder rehydrate(
            String id,
            String userId,
            String merchantOrgId,
            List<MallOrderLine> lines,
            MallOrder.Status status,
            PaymentIntent paymentIntent,
            Money discountTotal,
            Money paidAmount,
            Instant createdAt,
            Instant paidAt) {
        Objects.requireNonNull(lines, "lines");
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines 不能为空");
        }
        return new MallOrder(
                id,
                userId,
                merchantOrgId,
                lines,
                Objects.requireNonNull(status, "status"),
                paymentIntent,
                discountTotal,
                Objects.requireNonNull(paidAmount, "paidAmount"),
                Objects.requireNonNull(createdAt, "createdAt"),
                paidAt);
    }

    /** CREATED → PAID。调用方不得据此创建 Entitlement（INV-16）。 */
    public MallOrder pay(Instant at) {
        if (status != MallOrder.Status.CREATED) {
            throw new IllegalTransitionException(MallOrder.Status.PAID);
        }
        return new MallOrder(
                id,
                userId,
                merchantOrgId,
                lines,
                MallOrder.Status.PAID,
                paymentIntent,
                discountTotal,
                paidAmount,
                createdAt,
                Objects.requireNonNull(at, "paidAt"));
    }

    public boolean isPaid() {
        return status == MallOrder.Status.PAID;
    }

    public String id() {
        return id;
    }

    public String userId() {
        return userId;
    }

    public String merchantOrgId() {
        return merchantOrgId;
    }

    public List<MallOrderLine> lines() {
        return lines;
    }

    public MallOrder.Status status() {
        return status;
    }

    public PaymentIntent paymentIntent() {
        return paymentIntent;
    }

    public Money discountTotal() {
        return discountTotal;
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

    public static final class IllegalTransitionException extends RuntimeException {
        IllegalTransitionException(MallOrder.Status next) {
            super("非法商城订单状态迁移至 " + next);
        }
    }
}
