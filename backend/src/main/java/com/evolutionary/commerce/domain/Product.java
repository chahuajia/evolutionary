package com.evolutionary.commerce.domain;

import java.util.Objects;

public final class Product {

    private final String id;
    private final String orgId;
    private final String name;
    private final Money price;
    private final int durationDays;
    private final ProductStatus status;

    private Product(
            String id,
            String orgId,
            String name,
            Money price,
            int durationDays,
            ProductStatus status) {
        this.id = id;
        this.orgId = orgId;
        this.name = name;
        this.price = price;
        this.durationDays = durationDays;
        this.status = status;
    }

    public static Product create(
            String id,
            String orgId,
            String name,
            Money price,
            int durationDays,
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
                Objects.requireNonNull(status, "status"));
    }

    public boolean isPublished() {
        return status == ProductStatus.PUBLISHED;
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

    public ProductStatus status() {
        return status;
    }
}
