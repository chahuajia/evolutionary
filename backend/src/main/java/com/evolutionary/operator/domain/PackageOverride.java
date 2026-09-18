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

    /**
     * 套餐覆盖状态（对齐 IDL PackageOverride.Status）。
     */
    public enum Status {
        DRAFT,
        ACTIVE,
        REVOKED
    }


    private final String id;
    private final String orgId;
    private final String templateId;
    private final int templateVersion;
    private final List<OverridableField> allowedFields;
    private final OverridePatches patches;
    private final Instant effectiveFrom;
    private final Instant effectiveUntil;
    private final PackageOverride.Status status;

    private PackageOverride(
            String id,
            String orgId,
            String templateId,
            int templateVersion,
            List<OverridableField> allowedFields,
            OverridePatches patches,
            Instant effectiveFrom,
            Instant effectiveUntil,
            PackageOverride.Status status) {
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
                        PackageOverride.Status.DRAFT));
    }

    /**
     * 从持久化回放。
     *
     * <p>与 {@link #createDraft} 的区别：不校验"模板已发布 / 是后代组织 /
     * patches 非空"这些**创建时**的规则 —— 那些是写入路径的门禁，
     * 回放时它们早已通过。这里只校验形状。
     */
    public static PackageOverride rehydrate(
            String id,
            String orgId,
            String templateId,
            int templateVersion,
            List<OverridableField> allowedFields,
            OverridePatches patches,
            Instant effectiveFrom,
            Instant effectiveUntil,
            PackageOverride.Status status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("override id 不能为空");
        }
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId 不能为空");
        }
        Objects.requireNonNull(templateId, "templateId");
        Objects.requireNonNull(allowedFields, "allowedFields");
        Objects.requireNonNull(patches, "patches");
        Objects.requireNonNull(effectiveFrom, "effectiveFrom");
        Objects.requireNonNull(status, "status");
        return new PackageOverride(
                id,
                orgId,
                templateId,
                templateVersion,
                allowedFields,
                patches,
                effectiveFrom,
                effectiveUntil,
                status);
    }

    public OperatorOutcome<PackageOverride> activate() {
        if (status != PackageOverride.Status.DRAFT) {
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
                        PackageOverride.Status.ACTIVE));
    }

    public OperatorOutcome<PackageOverride> revoke() {
        if (status != PackageOverride.Status.ACTIVE) {
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
                        PackageOverride.Status.REVOKED));
    }

    public boolean isActive() {
        return status == PackageOverride.Status.ACTIVE;
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

    public PackageOverride.Status status() {
        return status;
    }
}
