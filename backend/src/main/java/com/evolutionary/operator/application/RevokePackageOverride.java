package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import com.evolutionary.operator.domain.OperatorErrorCode;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.PackageOverride;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * 撤销已激活的套餐覆盖（AC-31）。
 *
 * <p>撤销后目录回落至模板原价；记 AuditLog override.revoke。
 */
public final class RevokePackageOverride {

    private final PackageOverrideRepository overrides;
    private final AuditLogRepository auditLogs;
    private final Clock clock;

    public RevokePackageOverride(
            PackageOverrideRepository overrides, AuditLogRepository auditLogs, Clock clock) {
        this.overrides = Objects.requireNonNull(overrides, "overrides");
        this.auditLogs = Objects.requireNonNull(auditLogs, "auditLogs");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public OperatorOutcome<PackageOverride> execute(
            String actorUserId, String actorOrgId, String overrideId) {
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(actorOrgId, "actorOrgId");
        Objects.requireNonNull(overrideId, "overrideId");

        PackageOverride existing = overrides.get(overrideId);
        if (!actorOrgId.equals(existing.orgId())) {
            return OperatorOutcome.err(OperatorErrorCode.ORG_NOT_OWNER, "仅覆盖所属组织可撤销");
        }

        OperatorOutcome<PackageOverride> revoked = existing.revoke();
        if (revoked instanceof OperatorOutcome.Err<PackageOverride> err) {
            return err;
        }
        PackageOverride result = ((OperatorOutcome.Ok<PackageOverride>) revoked).value();
        overrides.save(result);

        auditLogs.append(
                AuditLog.of(
                        UUID.randomUUID().toString(),
                        actorUserId,
                        actorOrgId,
                        AuditAction.OVERRIDE_REVOKE,
                        "PackageOverride",
                        result.id(),
                        clock.instant()));

        return OperatorOutcome.ok(result);
    }
}
