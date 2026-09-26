package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.domain.OnboardingApplication;
import com.evolutionary.operator.domain.OrgCapability;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 入驻申请持久化 —— 只活在 infrastructure。 */
@Entity
@Table(name = "onboarding_applications")
public class OnboardingApplicationJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String orgId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgCapability capability;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnboardingApplication.Status status;

    @Column(nullable = false)
    private Instant submittedAt;

    /** 未审批时为 null。 */
    private Instant reviewedAt;

    protected OnboardingApplicationJpaEntity() {}

    public OnboardingApplicationJpaEntity(
            String id,
            String orgId,
            OrgCapability capability,
            OnboardingApplication.Status status,
            Instant submittedAt,
            Instant reviewedAt) {
        this.id = id;
        this.orgId = orgId;
        this.capability = capability;
        this.status = status;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
    }

    public String getId() {
        return id;
    }

    public String getOrgId() {
        return orgId;
    }

    public OrgCapability getCapability() {
        return capability;
    }

    public OnboardingApplication.Status getStatus() {
        return status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
