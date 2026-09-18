package com.evolutionary.operator.application;

import com.evolutionary.mall.application.MerchantProfileRepository;
import com.evolutionary.mall.domain.MerchantProfile;
import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import com.evolutionary.operator.domain.OnboardingApplication;
import com.evolutionary.operator.domain.OnboardingStatus;
import com.evolutionary.operator.domain.OperatorErrorCode;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.OrgCapability;
import com.evolutionary.operator.domain.Organization;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * 批准商家入驻（AC-40）。
 *
 * <p>批准后授予 {@link OrgCapability#MERCHANT} 并激活 {@link MerchantProfile}。
 * 仅 MERCHANT、无 OPERATOR 的组织不可发布 PackageTemplate。
 * 成功路径同事务记 {@link AuditAction#ONBOARDING_APPROVE}（切片30b）。
 */
public final class ApproveMerchantOnboarding {

    private final OnboardingApplicationRepository applications;
    private final OrganizationRepository organizations;
    private final MerchantProfileRepository merchants;
    private final AuditLogRepository auditLogs;
    private final Clock clock;

    public ApproveMerchantOnboarding(
            OnboardingApplicationRepository applications,
            OrganizationRepository organizations,
            MerchantProfileRepository merchants,
            AuditLogRepository auditLogs,
            Clock clock) {
        this.applications = Objects.requireNonNull(applications, "applications");
        this.organizations = Objects.requireNonNull(organizations, "organizations");
        this.merchants = Objects.requireNonNull(merchants, "merchants");
        this.auditLogs = Objects.requireNonNull(auditLogs, "auditLogs");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public OperatorOutcome<MerchantProfile> execute(
            String applicationId, String shopName, String actorUserId, String actorOrgId) {
        Objects.requireNonNull(applicationId, "applicationId");
        Objects.requireNonNull(shopName, "shopName");
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(actorOrgId, "actorOrgId");

        OnboardingApplication app = applications.get(applicationId);
        if (app.capability() != OrgCapability.MERCHANT) {
            return OperatorOutcome.err(
                    OperatorErrorCode.CAPABILITY_DENIED, "仅 MERCHANT 入驻可由本用例批准");
        }
        if (app.status() != OnboardingStatus.SUBMITTED) {
            return OperatorOutcome.err(
                    OperatorErrorCode.CAPABILITY_DENIED, "入驻申请非 submitted 状态");
        }

        OnboardingApplication approved = app.approve(clock.instant());
        applications.save(approved);

        Organization org = organizations.get(approved.orgId());
        Organization granted = org.grant(OrgCapability.MERCHANT);
        organizations.save(granted);

        MerchantProfile profile = MerchantProfile.activate(granted.id(), shopName);
        merchants.save(profile);

        auditLogs.append(
                AuditLog.of(
                        UUID.randomUUID().toString(),
                        actorUserId,
                        actorOrgId,
                        AuditAction.ONBOARDING_APPROVE,
                        "OnboardingApplication",
                        approved.id(),
                        clock.instant()));

        return OperatorOutcome.ok(profile);
    }
}
