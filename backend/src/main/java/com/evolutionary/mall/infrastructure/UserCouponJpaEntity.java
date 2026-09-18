package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.domain.UserCouponStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_coupons")
public class UserCouponJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String templateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserCouponStatus status;

    private String lockedByOrderId;

    protected UserCouponJpaEntity() {}

    public UserCouponJpaEntity(
            String id,
            String userId,
            String templateId,
            UserCouponStatus status,
            String lockedByOrderId) {
        this.id = id;
        this.userId = userId;
        this.templateId = templateId;
        this.status = status;
        this.lockedByOrderId = lockedByOrderId;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public UserCouponStatus getStatus() {
        return status;
    }

    public String getLockedByOrderId() {
        return lockedByOrderId;
    }
}
