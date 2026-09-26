package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.domain.OverridableField;
import com.evolutionary.operator.domain.PackageTemplate;
import com.evolutionary.operator.domain.TemplateBaseProduct;
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
 * 套餐模板持久化。
 *
 * <p>{@link TemplateBaseProduct} 是值对象（无标识），**展平进本表**而不是另建表 ——
 * 它随模板一起被替换，没有独立生命周期。
 */
@Entity
@Table(name = "package_templates")
public class PackageTemplateJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String ownerOrgId;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private String baseDisplayName;

    @Column(nullable = false)
    private long basePriceCents;

    @Column(nullable = false)
    private int baseDurationDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageTemplate.Status status;

    private String inheritedFrom;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "package_template_override_fields",
            joinColumns = @JoinColumn(name = "template_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "field", nullable = false)
    private List<OverridableField> allowedOverrideFields = new ArrayList<>();

    /** 草稿为 null。 */
    private Instant publishedAt;

    protected PackageTemplateJpaEntity() {}

    public PackageTemplateJpaEntity(
            String id,
            String ownerOrgId,
            int version,
            String baseDisplayName,
            long basePriceCents,
            int baseDurationDays,
            PackageTemplate.Status status,
            String inheritedFrom,
            List<OverridableField> allowedOverrideFields,
            Instant publishedAt) {
        this.id = id;
        this.ownerOrgId = ownerOrgId;
        this.version = version;
        this.baseDisplayName = baseDisplayName;
        this.basePriceCents = basePriceCents;
        this.baseDurationDays = baseDurationDays;
        this.status = status;
        this.inheritedFrom = inheritedFrom;
        this.allowedOverrideFields = new ArrayList<>(allowedOverrideFields);
        this.publishedAt = publishedAt;
    }

    public String getId() {
        return id;
    }

    public String getOwnerOrgId() {
        return ownerOrgId;
    }

    public int getVersion() {
        return version;
    }

    public String getBaseDisplayName() {
        return baseDisplayName;
    }

    public long getBasePriceCents() {
        return basePriceCents;
    }

    public int getBaseDurationDays() {
        return baseDurationDays;
    }

    public PackageTemplate.Status getStatus() {
        return status;
    }

    public String getInheritedFrom() {
        return inheritedFrom;
    }

    public List<OverridableField> getAllowedOverrideFields() {
        return List.copyOf(allowedOverrideFields);
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
