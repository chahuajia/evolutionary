package com.evolutionary.operator.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * L2 对已发布模板的字段覆盖（P3-3 / P3-4）。
 *
 * <p>激活不修改 Template；目录侧通过 {@link EffectiveProduct} 合成视图。
 */
public final class PackageOverride {

    private final String id;
    private final String orgId;
    private final String templateId;
    private final int templateVersion;
    private final List<OverridableField> allowedFields;
    private final OverridePatches patches;
    private final Instant effectiveFrom;
    private final Instant effectiveUntil;
    private final OverrideStatus status;

    private PackageOverride(
            String id,
            String orgId,
            String templateId,
            int templateVersion,
            List<OverridableField> allowedFields,
            OverridePatches patches,
            Instant effectiveFrom,
            Instant effectiveUntil,
            OverrideStatus status) {
        this.id = id;
        this.orgId = orgId;
        this.templateId = templateId;
        this.templateVersion = templateVersion;
        this.allowedFields = List.copyOf(allowedFields);
        this.patches = patches;
        this.effectiveFrom = effectiveFrom;
        this.effectiveUntil = effectiveUntil;
        this.status = status;
    }

    /**
     * 基于已发布模板创建草稿覆盖；patches 须已通过白名单解析。
     *
     * <p>后代组织校验由应用层 {@code OrgAuthorization} 负责。
     */
    public static OperatorOutcome<PackageOverride> createDraft(
            String id,
            String orgId,
            PackageTemplate template,
            OverridePatches patches,
            Instant effectiveFrom) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("override id 不能为空");
        }
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId 不能为空");
        }
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(patches, "patches");
        Objects.requireNonNull(effectiveFrom, "effectiveFrom");

        if (!template.isPublished()) {
            return OperatorOutcome.err(
                    OperatorErrorCode.TEMPLATE_NOT_PUBLISHED, "仅可对已发布模板创建覆盖");
        }
        if (patches.isEmpty()) {
            return OperatorOutcome.err(OperatorErrorCode.OVERRIDE_INVALID, "patches 不能为空");
        }
        if (orgId.equals(template.ownerOrgId())) {
            return OperatorOutcome.err(
                    OperatorErrorCode.ORG_NOT_DESCENDANT, "归属组织自身不创建覆盖，请改模板版本");
        }

        return OperatorOutcome.ok(
                new PackageOverride(
                        id,
                        orgId,
                        template.id(),
                        template.version(),
                        template.allowedOverrideFields(),
                        patches,
                        effectiveFrom,
                        null,
                        OverrideStatus.DRAFT));
    }

    public OperatorOutcome<PackageOverride> activate() {
        if (status != OverrideStatus.DRAFT) {
            return OperatorOutcome.err(OperatorErrorCode.OVERRIDE_INVALID, "仅草稿可激活");
        }
        return OperatorOutcome.ok(
                new PackageOverride(
                        id,
                        orgId,
                        templateId,
                        templateVersion,
                        allowedFields,
                        patches,
                        effectiveFrom,
                        effectiveUntil,
                        OverrideStatus.ACTIVE));
    }

    public OperatorOutcome<PackageOverride> revoke() {
        if (status != OverrideStatus.ACTIVE) {
            return OperatorOutcome.err(OperatorErrorCode.OVERRIDE_INVALID, "仅激活态可撤销");
        }
        return OperatorOutcome.ok(
                new PackageOverride(
                        id,
                        orgId,
                        templateId,
                        templateVersion,
                        allowedFields,
                        patches,
                        effectiveFrom,
                        effectiveUntil,
                        OverrideStatus.REVOKED));
    }

    public boolean isActive() {
        return status == OverrideStatus.ACTIVE;
    }

    public String id() {
        return id;
    }

    public String orgId() {
        return orgId;
    }

    public String templateId() {
        return templateId;
    }

    public int templateVersion() {
        return templateVersion;
    }

    public List<OverridableField> allowedFields() {
        return allowedFields;
    }

    public OverridePatches patches() {
        return patches;
    }

    public Instant effectiveFrom() {
        return effectiveFrom;
    }

    public Instant effectiveUntil() {
        return effectiveUntil;
    }

    public OverrideStatus status() {
        return status;
    }
}
