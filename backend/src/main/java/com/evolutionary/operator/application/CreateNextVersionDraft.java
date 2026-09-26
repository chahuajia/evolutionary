package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import com.evolutionary.operator.domain.OperatorErrorCode;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.Organization;
import com.evolutionary.operator.domain.PackageTemplate;
import com.evolutionary.operator.domain.TemplateBaseProduct;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * 从已发布模板派生下一版本草稿（AC-25 合法变更路径）。
 *
 * <p>对齐 {@link PackageTemplate#createNextVersionDraft}；仅 owner + OPERATOR。
 */
public final class CreateNextVersionDraft {

    private final PackageTemplateRepository templates;
    private final AuditLogRepository auditLogs;
    private final OrganizationRepository organizations;
    private final Clock clock;

    public CreateNextVersionDraft(
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
            String actorUserId,
            String actorOrgId,
            String sourceTemplateId,
            String newTemplateId,
            TemplateBaseProduct nextBase) {
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(actorOrgId, "actorOrgId");
        Objects.requireNonNull(sourceTemplateId, "sourceTemplateId");
        Objects.requireNonNull(newTemplateId, "newTemplateId");
        Objects.requireNonNull(nextBase, "nextBase");

        PackageTemplate source = templates.get(sourceTemplateId);
        if (!actorOrgId.equals(source.ownerOrgId())) {
            return OperatorOutcome.err(OperatorErrorCode.ORG_NOT_OWNER, "仅归属组织可派生下一版本");
        }

        Organization org = organizations.get(actorOrgId);
        if (!org.canPublishPackageTemplate()) {
            return OperatorOutcome.err(
                    OperatorErrorCode.CAPABILITY_DENIED, "仅 OPERATOR 可派生套餐模板");
        }

        OperatorOutcome<PackageTemplate> drafted =
                source.createNextVersionDraft(newTemplateId.trim(), nextBase);
        if (drafted instanceof OperatorOutcome.Err<PackageTemplate> err) {
            return err;
        }
        PackageTemplate result = ((OperatorOutcome.Ok<PackageTemplate>) drafted).value();
        templates.save(result);

        auditLogs.append(
                AuditLog.of(
                        UUID.randomUUID().toString(),
                        actorUserId,
                        actorOrgId,
                        AuditAction.TEMPLATE_NEXT_VERSION,
                        "PackageTemplate",
                        result.id(),
                        clock.instant()));

        return OperatorOutcome.ok(result);
    }
}
