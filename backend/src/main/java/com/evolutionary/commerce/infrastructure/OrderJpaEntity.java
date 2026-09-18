package com.evolutionary.commerce.infrastructure;


import com.evolutionary.commerce.domain.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String orgId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.Status status;

    @Column(nullable = false)
    private long paidCents;

    @Column(nullable = false)
    private String paidCurrency;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant paidAt;

    private Instant refundedAt;

    protected OrderJpaEntity() {}

    public OrderJpaEntity(
            String id,
            String userId,
            String productId,
            String orgId,
            Order.Status status,
            long paidCents,
            String paidCurrency,
            Instant createdAt,
            Instant paidAt,
            Instant refundedAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.orgId = orgId;
        this.status = status;
        this.paidCents = paidCents;
        this.paidCurrency = paidCurrency;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
        this.refundedAt = refundedAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public String getOrgId() {
        return orgId;
    }

    public Order.Status getStatus() {
        return status;
    }

    public long getPaidCents() {
        return paidCents;
    }

    public String getPaidCurrency() {
        return paidCurrency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }
}
