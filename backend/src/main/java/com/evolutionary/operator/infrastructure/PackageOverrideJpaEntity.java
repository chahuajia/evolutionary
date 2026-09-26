package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.domain.OverridableField;
import com.evolutionary.operator.domain.PackageOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 套餐覆盖持久化。
 *
 * <p>{@code OverridePatches} 是 {@code (Long priceCents, String displayName)} 二元组，
 * 展平为两列 —— {@code priceCents} 用**可空 Long**：null 表示"不覆盖该字段"，
 * 与 0 是两回事（见 [[patterns/value-semantics]]）。
 */
@Entity
@Table(name = "package_overrides")
public class PackageOverrideJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String orgId;

    @Column(nullable = false)
    private String templateId;

    @Column(nullable = false)
    private int templateVersion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "package_override_fields",
            joinColumns = @JoinColumn(name = "override_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "field", nullable = false)
    private List<OverridableField> allowedFields = new ArrayList<>();

    /** null = 不覆盖价格（不是 0）。 */
    private Long patchPriceCents;

    /** null = 不覆盖名称（不是空串）。 */
    private String patchDisplayName;

    @Column(nullable = false)
    private Instant effectiveFrom;

    private Instant effectiveUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageOverride.Status status;

    protected PackageOverrideJpaEntity() {}

    public PackageOverrideJpaEntity(
            String id,
            String orgId,
            String templateId,
            int templateVersion,
            List<OverridableField> allowedFields,
            Long patchPriceCents,
            String patchDisplayName,
            Instant effectiveFrom,
            Instant effectiveUntil,
            PackageOverride.Status status) {
        this.id = id;
        this.orgId = orgId;
        this.templateId = templateId;
        this.templateVersion = templateVersion;
        this.allowedFields = new ArrayList<>(allowedFields);
        this.patchPriceCents = patchPriceCents;
        this.patchDisplayName = patchDisplayName;
        this.effectiveFrom = effectiveFrom;
        this.effectiveUntil = effectiveUntil;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public int getTemplateVersion() {
        return templateVersion;
    }

    public List<OverridableField> getAllowedFields() {
        return List.copyOf(allowedFields);
    }

    public Long getPatchPriceCents() {
        return patchPriceCents;
    }

    public String getPatchDisplayName() {
        return patchDisplayName;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public Instant getEffectiveUntil() {
        return effectiveUntil;
    }

    public PackageOverride.Status getStatus() {
        return status;
    }
}
