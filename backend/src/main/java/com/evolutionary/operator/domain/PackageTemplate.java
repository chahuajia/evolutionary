package com.evolutionary.operator.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 套餐模板聚合（P3-2）。
 *
 * <p>状态机：draft --publish--> published；published 内容不可原地 mutate，变更须新建 version+1 草稿。
 */
public final class PackageTemplate {

    /**
     * 套餐模板状态（对齐 IDL PackageTemplate.Status）。
     */
    public enum Status {
        DRAFT,
        PUBLISHED,
        DEPRECATED
    }


    private final String id;
    private final String ownerOrgId;
    private final int version;
    private final TemplateBaseProduct baseProduct;
    private final PackageTemplate.Status status;
    private final String inheritedFrom;
    private final List<OverridableField> allowedOverrideFields;
    private final Instant publishedAt;

    private PackageTemplate(
            String id,
            String ownerOrgId,
            int version,
            TemplateBaseProduct baseProduct,
            PackageTemplate.Status status,
            String inheritedFrom,
            List<OverridableField> allowedOverrideFields,
            Instant publishedAt) {
        this.id = id;
        this.ownerOrgId = ownerOrgId;
        this.version = version;
        this.baseProduct = baseProduct;
        this.status = status;
        this.inheritedFrom = inheritedFrom;
        this.allowedOverrideFields = List.copyOf(allowedOverrideFields);
        this.publishedAt = publishedAt;
    }

    /** L1 创建草稿（version 从 1 起，发布后仍为 1）。 */
    public static PackageTemplate createDraft(
            String id,
            String ownerOrgId,
            TemplateBaseProduct baseProduct,
            List<OverridableField> allowedOverrideFields) {
        return createDraft(id, ownerOrgId, 1, baseProduct, null, allowedOverrideFields);
    }

    public static PackageTemplate createDraft(
            String id,
            String ownerOrgId,
            int version,
            TemplateBaseProduct baseProduct,
            String inheritedFrom,
            List<OverridableField> allowedOverrideFields) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("template id 不能为空");
        }
        if (ownerOrgId == null || ownerOrgId.isBlank()) {
            throw new IllegalArgumentException("ownerOrgId 不能为空");
        }
        if (version < 1) {
            throw new IllegalArgumentException("version 必须 >= 1");
        }
        Objects.requireNonNull(baseProduct, "baseProduct");
        Objects.requireNonNull(allowedOverrideFields, "allowedOverrideFields");
        return new PackageTemplate(
                id,
                ownerOrgId,
                version,
                baseProduct,
                PackageTemplate.Status.DRAFT,
                inheritedFrom,
                allowedOverrideFields,
                null);
    }

    /**
     * 从持久化回放。
     *
     * <p>与 {@link #createDraft} 的区别：**不强制 status 为 DRAFT** ——
     * 库里存着的就是它当时的那个状态（`published` 带 `publishedAt`，
     * 草稿的 `publishedAt` 为 null）。校验的是形状，不是状态机。
     */
    public static PackageTemplate rehydrate(
            String id,
            String ownerOrgId,
            int version,
            TemplateBaseProduct baseProduct,
            PackageTemplate.Status status,
            String inheritedFrom,
            List<OverridableField> allowedOverrideFields,
            Instant publishedAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("template id 不能为空");
        }
        if (ownerOrgId == null || ownerOrgId.isBlank()) {
            throw new IllegalArgumentException("ownerOrgId 不能为空");
        }
        if (version < 1) {
            throw new IllegalArgumentException("version 必须 >= 1");
        }
        Objects.requireNonNull(baseProduct, "baseProduct");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(allowedOverrideFields, "allowedOverrideFields");
        return new PackageTemplate(
                id,
                ownerOrgId,
                version,
                baseProduct,
                status,
                inheritedFrom,
                allowedOverrideFields,
                publishedAt);
    }

    /** draft → published；记录 publishedAt。 */
    public OperatorOutcome<PackageTemplate> publish(Instant at) {
        Objects.requireNonNull(at, "at");
        if (status != PackageTemplate.Status.DRAFT) {
            return OperatorOutcome.err(OperatorErrorCode.TEMPLATE_NOT_DRAFT, "仅草稿可发布");
        }
        return OperatorOutcome.ok(
                new PackageTemplate(
                        id,
                        ownerOrgId,
                        version,
                        baseProduct,
                        PackageTemplate.Status.PUBLISHED,
                        inheritedFrom,
                        allowedOverrideFields,
                        at));
    }

    /**
     * 试图原地改 baseProduct。published / deprecated 一律拒绝（TEMPLATE_IMMUTABLE）。
     */
    public OperatorOutcome<PackageTemplate> replaceBaseProduct(TemplateBaseProduct next) {
        Objects.requireNonNull(next, "next");
        if (status != PackageTemplate.Status.DRAFT) {
            return OperatorOutcome.err(
                    OperatorErrorCode.TEMPLATE_IMMUTABLE, "已发布模板不可原地修改，请新建下一版本草稿");
        }
        return OperatorOutcome.ok(
                new PackageTemplate(
                        id,
                        ownerOrgId,
                        version,
                        next,
                        status,
                        inheritedFrom,
                        allowedOverrideFields,
                        publishedAt));
    }

    /**
     * 从已发布模板派生下一版本草稿（version+1），用于合法变更。
     *
     * <p>新草稿使用新 id；同源关系由调用方在仓储层关联（本切片不强制）。
     */
    public OperatorOutcome<PackageTemplate> createNextVersionDraft(
            String newId, TemplateBaseProduct nextBase) {
        Objects.requireNonNull(nextBase, "nextBase");
        if (status != PackageTemplate.Status.PUBLISHED) {
            return OperatorOutcome.err(
                    OperatorErrorCode.TEMPLATE_NOT_PUBLISHED, "仅已发布模板可派生下一版本");
        }
        return OperatorOutcome.ok(
                createDraft(
                        newId,
                        ownerOrgId,
                        version + 1,
                        nextBase,
                        inheritedFrom,
                        allowedOverrideFields));
    }

    public boolean isPublished() {
        return status == PackageTemplate.Status.PUBLISHED;
    }

    public boolean isDraft() {
        return status == PackageTemplate.Status.DRAFT;
    }

    public String id() {
        return id;
    }

    public String ownerOrgId() {
        return ownerOrgId;
    }

    public int version() {
        return version;
    }

    public TemplateBaseProduct baseProduct() {
        return baseProduct;
    }

    public PackageTemplate.Status status() {
        return status;
    }

    public String inheritedFrom() {
        return inheritedFrom;
    }

    public List<OverridableField> allowedOverrideFields() {
        return allowedOverrideFields;
    }

    public Instant publishedAt() {
        return publishedAt;
    }
}
