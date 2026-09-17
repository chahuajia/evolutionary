package com.evolutionary.settlement.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * 分润意向（追加-only）。
 *
 * <p>ORDER_COMPLETED 时写 PENDING；结算 / 退款 reversal 见后续切片（规格 §4）。
 */
public final class ProfitShareAccrual {

    private final String id;
    private final String orderId;
    private final String orgId;
    private final long amountCents;
    private final String currency;
    private final int ruleVersion;
    private final AccrualStatus status;
    private final Instant createdAt;
    private final Instant settledAt;
    private final String batchId;
    private final String reversalOf;

    private ProfitShareAccrual(
            String id,
            String orderId,
            String orgId,
            long amountCents,
            String currency,
            int ruleVersion,
            AccrualStatus status,
            Instant createdAt,
            Instant settledAt,
            String batchId,
            String reversalOf) {
        this.id = id;
        this.orderId = orderId;
        this.orgId = orgId;
        this.amountCents = amountCents;
        this.currency = currency;
        this.ruleVersion = ruleVersion;
        this.status = status;
        this.createdAt = createdAt;
        this.settledAt = settledAt;
        this.batchId = batchId;
        this.reversalOf = reversalOf;
    }

    /** 订单完成后记一笔 PENDING 意向。 */
    public static ProfitShareAccrual pending(
            String id,
            String orderId,
            String orgId,
            long amountCents,
            String currency,
            int ruleVersion,
            Instant createdAt) {
        if (amountCents < 0) {
            throw new IllegalArgumentException("金额不能为负");
        }
        return new ProfitShareAccrual(
                requireId(id),
                requireId(orderId),
                requireId(orgId),
                amountCents,
                requireCurrency(currency),
                ruleVersion,
                AccrualStatus.PENDING,
                Objects.requireNonNull(createdAt, "createdAt"),
                null,
                null,
                null);
    }

    public String id() {
        return id;
    }

    public String orderId() {
        return orderId;
    }

    public String orgId() {
        return orgId;
    }

    public long amountCents() {
        return amountCents;
    }

    public String currency() {
        return currency;
    }

    public int ruleVersion() {
        return ruleVersion;
    }

    public AccrualStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant settledAt() {
        return settledAt;
    }

    public String batchId() {
        return batchId;
    }

    public String reversalOf() {
        return reversalOf;
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id 不能为空");
        }
        return id;
    }

    private static String requireCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency 不能为空");
        }
        return currency;
    }
}
