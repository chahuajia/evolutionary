package com.evolutionary.operator.domain;

import java.time.Instant;
import java.util.Objects;

/** 能力入驻申请（运营商/商家共享框架）。 */
public final class OnboardingApplication {

    /**
     * 入驻申请状态。
     */
    public enum Status {
        SUBMITTED,
        APPROVED,
        REJECTED
    }


    private final String id;
    private final String orgId;
    private final OrgCapability capability;
    private final OnboardingApplication.Status status;
    private final Instant submittedAt;
    private final Instant reviewedAt;

    private OnboardingApplication(
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

    public static OnboardingApplication submit(
            String id, String orgId, OrgCapability capability, Instant submittedAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id 不能为空");
        }
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId 不能为空");
        }
        Objects.requireNonNull(capability, "capability");
        return new OnboardingApplication(
                id,
                orgId,
                capability,
                OnboardingApplication.Status.SUBMITTED,
                Objects.requireNonNull(submittedAt, "submittedAt"),
                null);
    }

    public OnboardingApplication approve(Instant reviewedAt) {
        if (status != OnboardingApplication.Status.SUBMITTED) {
            throw new IllegalStateException("仅 submitted 可批准");
        }
        return new OnboardingApplication(
                id,
                orgId,
                capability,
                OnboardingApplication.Status.APPROVED,
                submittedAt,
                Objects.requireNonNull(reviewedAt, "reviewedAt"));
    }

    public String id() {
        return id;
    }

    public String orgId() {
        return orgId;
    }

    public OrgCapability capability() {
        return capability;
    }

    public OnboardingApplication.Status status() {
        return status;
    }

    public Instant submittedAt() {
        return submittedAt;
    }

    public Instant reviewedAt() {
        return reviewedAt;
    }
}
