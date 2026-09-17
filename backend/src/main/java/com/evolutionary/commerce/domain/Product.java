package com.evolutionary.commerce.domain;

import java.util.Objects;

public final class Product {

    private final String id;
    private final String orgId;
    private final String name;
    private final Money price;
    private final int durationDays;
    private final SwapLimit swapLimit;
    private final ProductStatus status;
    /** null = FIXED_PRICE；非 null = METERED 单价（每 SOC 单位）。 */
    private final Money meteredRate;

    private Product(
            String id,
            String orgId,
            String name,
            Money price,
            int durationDays,
            SwapLimit swapLimit,
            ProductStatus status,
            Money meteredRate) {
        this.id = id;
        this.orgId = orgId;
        this.name = name;
        this.price = price;
        this.durationDays = durationDays;
        this.swapLimit = swapLimit;
        this.status = status;
        this.meteredRate = meteredRate;
    }

    /** phase-0 兼容：UNLIMITED 次卡。 */
    public static Product create(
            String id,
            String orgId,
            String name,
            Money price,
            int durationDays,
            ProductStatus status) {
        return create(id, orgId, name, price, durationDays, SwapLimit.unlimited(), status);
    }

    public static Product create(
            String id,
            String orgId,
            String name,
            Money price,
            int durationDays,
            SwapLimit swapLimit,
            ProductStatus status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("product id must not be blank");
        }
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId must not be blank");
        }
        if (durationDays <= 0) {
            throw new IllegalArgumentException("durationDays must be positive");
        }
        return new Product(
                id,
                orgId,
                name,
                Objects.requireNonNull(price, "price"),
                durationDays,
                Objects.requireNonNull(swapLimit, "swapLimit"),
                Objects.requireNonNull(status, "status"),
                null);
    }

    /** phase-1 P3：METERED { unit: SOC, rate }。 */
    public static Product createMetered(
            String id, String orgId, String name, Money ratePerSocUnit, ProductStatus status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("product id must not be blank");
        }
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId must not be blank");
        }
        Objects.requireNonNull(ratePerSocUnit, "ratePerSocUnit");
        if (ratePerSocUnit.cents() <= 0) {
            throw new IllegalArgumentException("metered rate must be positive");
        }
        return new Product(
                id,
                orgId,
                name,
                new Money(0, ratePerSocUnit.currency()),
                1,
                SwapLimit.unlimited(),
                Objects.requireNonNull(status, "status"),
                ratePerSocUnit);
    }

    public boolean isPublished() {
        return status == ProductStatus.PUBLISHED;
    }

    public boolean isMetered() {
        return meteredRate != null;
    }

    public String id() {
        return id;
    }

    public String orgId() {
        return orgId;
    }

    public String name() {
        return name;
    }

    public Money price() {
        return price;
    }

    public int durationDays() {
        return durationDays;
    }

    public SwapLimit swapLimit() {
        return swapLimit;
    }

    public ProductStatus status() {
        return status;
    }

    public Money meteredRate() {
        return meteredRate;
    }
}
