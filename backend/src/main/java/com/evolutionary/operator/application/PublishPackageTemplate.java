package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import com.evolutionary.operator.domain.OperatorErrorCode;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.Organization;
import com.evolutionary.operator.domain.PackageTemplate;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * 发布套餐模板（AC-24）。
 *
 * <p>仅 ownerOrg 可发布；须具备 OPERATOR 能力（AC-40：纯 MERCHANT 禁发）；
 * published 后内容不可变（AC-25 由领域守卫）。
 */
public final class PublishPackageTemplate {

    private final PackageTemplateRepository templates;
    private final AuditLogRepository auditLogs;
    private final OrganizationRepository organizations;
    private final Clock clock;

    public PublishPackageTemplate(
            PackageTemplateRepository templates,
            AuditLogRepository auditLogs,
            OrganizationRepository organizations,
            Clock clock) {
        this.templates = Objects.requireNonNull(templates, "templates");
        this.auditLogs = Objects.requireNonNull(auditLogs, "auditLogs");
        this.organizations = Objects.requireNonNull(organizations, "organizations");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public OperatorOutcome<PackageTemplate> execute(
            String actorUserId, String actorOrgId, String templateId) {
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(actorOrgId, "actorOrgId");
        Objects.requireNonNull(templateId, "templateId");

        PackageTemplate draft = templates.get(templateId);
        if (!actorOrgId.equals(draft.ownerOrgId())) {
            return OperatorOutcome.err(OperatorErrorCode.ORG_NOT_OWNER, "仅归属组织可发布模板");
        }

        Organization org = organizations.get(actorOrgId);
        if (!org.canPublishPackageTemplate()) {
            return OperatorOutcome.err(
                    OperatorErrorCode.CAPABILITY_DENIED, "仅 OPERATOR 可发布套餐模板");
        }

        OperatorOutcome<PackageTemplate> published = draft.publish(clock.instant());
        if (published instanceof OperatorOutcome.Err<PackageTemplate> err) {
            return err;
        }
        PackageTemplate result = ((OperatorOutcome.Ok<PackageTemplate>) published).value();
        templates.save(result);

        auditLogs.append(
                AuditLog.of(
                        UUID.randomUUID().toString(),
                        actorUserId,
                        actorOrgId,
                        AuditAction.TEMPLATE_PUBLISH,
                        "PackageTemplate",
                        result.id(),
                        clock.instant()));

        return OperatorOutcome.ok(result);
    }
}
