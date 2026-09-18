package com.evolutionary.commerce.infrastructure;


import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.MeteringMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 权益持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "entitlements")
public class EntitlementJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private Instant validFrom;

    /** null = PAY_AS_YOU_GO 无窗口截止。 */
    private Instant validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Entitlement.Status status;

    /** null = UNLIMITED。 */
    private Integer remainingSwaps;

    /** null = 非计量。 */
    @Enumerated(EnumType.STRING)
    private MeteringMode meteringMode;

    protected EntitlementJpaEntity() {}

    public EntitlementJpaEntity(
            String id,
            String orderId,
            String userId,
            String productId,
            Instant validFrom,
            Instant validUntil,
            Entitlement.Status status,
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

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public Entitlement.Status getStatus() {
        return status;
    }

    public Integer getRemainingSwaps() {
        return remainingSwaps;
    }

    public MeteringMode getMeteringMode() {
        return meteringMode;
    }
}
