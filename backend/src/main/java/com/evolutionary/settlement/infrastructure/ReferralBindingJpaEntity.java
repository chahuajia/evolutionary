package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.domain.ReferralStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 推广绑定持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "referral_bindings")
public class ReferralBindingJpaEntity {

    @Id
    private String userId;

    @Column(nullable = false)
    private String promoterOrgId;

    @Column(nullable = false)
    private Instant boundAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralStatus status;

    protected ReferralBindingJpaEntity() {}

    public ReferralBindingJpaEntity(
            String userId,
            String promoterOrgId,
            Instant boundAt,
            Instant expiresAt,
            ReferralStatus status) {
        this.userId = userId;
        this.promoterOrgId = promoterOrgId;
        this.boundAt = boundAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public String getPromoterOrgId() {
        return promoterOrgId;
    }

    public Instant getBoundAt() {
        return boundAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public ReferralStatus getStatus() {
        return status;
    }
}
