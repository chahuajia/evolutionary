package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.OnboardingApplication;
import com.evolutionary.operator.domain.OperatorErrorCode;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.OrgCapability;
import com.evolutionary.operator.domain.Organization;
import java.time.Clock;
import java.util.Objects;

/**
 * 运营商批准下线 OPERATOR 入驻（切片29a）。
 *
 * <p>与平台批 MERCHANT（{@link ApproveMerchantOnboarding} / {@code /admin}）分权：本用例仅接受
 * OPERATOR 申请；批准后挂 parent=actorOrg，并授予 OPERATOR。
 */
public final class ApproveOperatorDownline {

    private final OnboardingApplicationRepository applications;
    private final OrganizationRepository organizations;
    private final Clock clock;

    public ApproveOperatorDownline(
            OnboardingApplicationRepository applications,
            OrganizationRepository organizations,
            Clock clock) {
        this.applications = Objects.requireNonNull(applications, "applications");
        this.organizations = Objects.requireNonNull(organizations, "organizations");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public OperatorOutcome<Organization> execute(
            String actorUserId, String actorOrgId, String applicationId) {
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(actorOrgId, "actorOrgId");
        Objects.requireNonNull(applicationId, "applicationId");

        Organization actor = organizations.get(actorOrgId);
        if (!actor.hasCapability(OrgCapability.OPERATOR)) {
            return OperatorOutcome.err(
                    OperatorErrorCode.CAPABILITY_DENIED, "仅 OPERATOR 组织可批下线");
        }

        OnboardingApplication app = applications.get(applicationId);
        if (app.capability() != OrgCapability.OPERATOR) {
            return OperatorOutcome.err(
                    OperatorErrorCode.CAPABILITY_DENIED, "仅 OPERATOR 下线入驻可由运营商批准");
        }
        if (app.status() != OnboardingApplication.Status.SUBMITTED) {
            return OperatorOutcome.err(
                    OperatorErrorCode.CAPABILITY_DENIED, "入驻申请非 submitted 状态");
        }

        Organization pending = organizations.get(app.orgId());
        if (pending.parentId() != null && !pending.parentId().equals(actorOrgId)) {
            return OperatorOutcome.err(
                    OperatorErrorCode.CAPABILITY_DENIED, "仅可批准本组织发展的下线");
        }

        OnboardingApplication approved = app.approve(clock.instant());
        applications.save(approved);

        Organization linked =
                pending.parentId() == null ? pending.withParent(actorOrgId) : pending;
        Organization granted = linked.grant(OrgCapability.OPERATOR);
        organizations.save(granted);
        return OperatorOutcome.ok(granted);
    }
}
