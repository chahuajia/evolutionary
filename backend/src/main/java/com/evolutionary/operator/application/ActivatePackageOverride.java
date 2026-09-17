package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import com.evolutionary.operator.domain.OperatorErrorCode;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.OrgAuthorization;
import com.evolutionary.operator.domain.OverridePatches;
import com.evolutionary.operator.domain.PackageOverride;
import com.evolutionary.operator.domain.PackageTemplate;
import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * L2 创建并激活套餐覆盖（AC-26 / AC-27）。
 *
 * <p>仅模板 owner 的后代可操作；patches key 须 ⊆ 模板 allowedOverrideFields。
 */
public final class ActivatePackageOverride {

    private final PackageTemplateRepository templates;
    private final PackageOverrideRepository overrides;
    private final AuditLogRepository auditLogs;
    private final OrgAuthorization orgAuthorization;
    private final Clock clock;

    public ActivatePackageOverride(
            PackageTemplateRepository templates,
            PackageOverrideRepository overrides,
            AuditLogRepository auditLogs,
            OrgAuthorization orgAuthorization,
            Clock clock) {
        this.templates = Objects.requireNonNull(templates, "templates");
        this.overrides = Objects.requireNonNull(overrides, "overrides");
        this.auditLogs = Objects.requireNonNull(auditLogs, "auditLogs");
        this.orgAuthorization = Objects.requireNonNull(orgAuthorization, "orgAuthorization");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public OperatorOutcome<PackageOverride> execute(
            String actorUserId,
            String actorOrgId,
            String templateId,
            String overrideId,
            Map<String, ?> rawPatches) {
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(actorOrgId, "actorOrgId");
        Objects.requireNonNull(templateId, "templateId");
        Objects.requireNonNull(overrideId, "overrideId");
        Objects.requireNonNull(rawPatches, "rawPatches");

        PackageTemplate template = templates.get(templateId);
        if (!template.isPublished()) {
            return OperatorOutcome.err(
                    OperatorErrorCode.TEMPLATE_NOT_PUBLISHED, "仅可对已发布模板激活覆盖");
        }
        if (!orgAuthorization.isDescendant(actorOrgId, template.ownerOrgId())) {
            return OperatorOutcome.err(
                    OperatorErrorCode.ORG_NOT_DESCENDANT, "仅模板归属组织的后代可创建覆盖");
        }

        OperatorOutcome<OverridePatches> parsed =
                OverridePatches.fromRaw(rawPatches, template.allowedOverrideFields());
        if (parsed instanceof OperatorOutcome.Err<OverridePatches> err) {
            return OperatorOutcome.err(err.code(), err.message());
        }
        OverridePatches patches = ((OperatorOutcome.Ok<OverridePatches>) parsed).value();

        OperatorOutcome<PackageOverride> drafted =
                PackageOverride.createDraft(
                        overrideId, actorOrgId, template, patches, clock.instant());
        if (drafted instanceof OperatorOutcome.Err<PackageOverride> err) {
            return err;
        }
        PackageOverride draft = ((OperatorOutcome.Ok<PackageOverride>) drafted).value();

        OperatorOutcome<PackageOverride> activated = draft.activate();
        if (activated instanceof OperatorOutcome.Err<PackageOverride> err) {
            return err;
        }
        PackageOverride active = ((OperatorOutcome.Ok<PackageOverride>) activated).value();
        overrides.save(active);

        auditLogs.append(
                AuditLog.of(
                        UUID.randomUUID().toString(),
                        actorUserId,
                        actorOrgId,
                        AuditAction.OVERRIDE_ACTIVATE,
                        "PackageOverride",
                        active.id(),
                        clock.instant()));

        return OperatorOutcome.ok(active);
    }
}
