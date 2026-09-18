package com.evolutionary.mall.domain;

import com.evolutionary.commerce.domain.Money;
import java.util.Objects;

/**
 * 商城 SKU（与换电 Product 分离，避免 Entitlement 语义污染）。
 *
 * <p>仅依赖 {@code merchantOrgId} 字符串，可与 Merchant 入驻切片并行。
 */
public final class MallSku {

    private final String id;
    private final String merchantOrgId;
    private final String name;
    private final Money price;
    private final int stock;
    private final MallSkuStatus status;

    private MallSku(
            String id,
            String merchantOrgId,
            String name,
            Money price,
            int stock,
            MallSkuStatus status) {
        this.id = id;
        this.merchantOrgId = merchantOrgId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    /** JPA 回放；不做业务校验。 */
    public static MallSku rehydrate(
            String id,
            String merchantOrgId,
            String name,
            Money price,
            int stock,
            MallSkuStatus status) {
        return new MallSku(
                id,
                merchantOrgId,
                name,
                Objects.requireNonNull(price, "price"),
                stock,
                Objects.requireNonNull(status, "status"));
    }

    public static MallSku createOnSale(
            String id, String merchantOrgId, String name, Money price, int stock) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("sku id 不能为空");
        }
        if (merchantOrgId == null || merchantOrgId.isBlank()) {
            throw new IllegalArgumentException("merchantOrgId 不能为空");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name 不能为空");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock 不能为负");
        }
        return new MallSku(
                id,
                merchantOrgId,
                name,
                Objects.requireNonNull(price, "price"),
                stock,
                MallSkuStatus.ON_SALE);
    }

    public boolean isOnSale() {
        return status == MallSkuStatus.ON_SALE;
    }

    /** 扣减库存；不足时返回错误。 */
    public MallOutcome<MallSku> deductStock(int qty) {
        if (qty <= 0) {
            return MallOutcome.err(MallErrorCode.INVALID_QTY, "qty 必须为正");
        }
        if (!isOnSale()) {
            return MallOutcome.err(MallErrorCode.SKU_NOT_ON_SALE, "SKU 未上架");
        }
        if (stock < qty) {
            return MallOutcome.err(MallErrorCode.INSUFFICIENT_STOCK, "库存不足");
        }
        return MallOutcome.ok(new MallSku(id, merchantOrgId, name, price, stock - qty, status));
    }

    public MallSku takeOffSale() {
        return new MallSku(id, merchantOrgId, name, price, stock, MallSkuStatus.OFF_SALE);
    }

    public String id() {
        return id;
    }

    public String merchantOrgId() {
        return merchantOrgId;
    }

    public String name() {
        return name;
    }

    public Money price() {
        return price;
    }

    public int stock() {
        return stock;
    }

    public MallSkuStatus status() {
        return status;
    }
}
