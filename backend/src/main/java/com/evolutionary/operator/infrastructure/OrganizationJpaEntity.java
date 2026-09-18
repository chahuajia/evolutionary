package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.domain.OrgCapability;
import com.evolutionary.operator.domain.Organization;
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
import java.util.ArrayList;
import java.util.List;

/** 组织持久化 —— 只活在 infrastructure。 */
@Entity
@Table(name = "organizations")
public class OrganizationJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    /** 根组织为 null。 */
    private String parentId;

    /**
     * 能力集合。
     *
     * <p>{@code EAGER}：{@link com.evolutionary.operator.domain.OrgAuthorization} 在
     * bean 构造时一次性读全表建索引，懒加载会在此后失效（见 `HANDOVER.md` 的种子时机一节）。
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "organization_capabilities",
            joinColumns = @JoinColumn(name = "org_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "capability", nullable = false)
    private List<OrgCapability> capabilities = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_regions", joinColumns = @JoinColumn(name = "org_id"))
    @Column(name = "region", nullable = false)
    private List<String> regionScope = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Organization.Status status;

    protected OrganizationJpaEntity() {}

    public OrganizationJpaEntity(
            String id,
            String name,
            String parentId,
            List<OrgCapability> capabilities,
            List<String> regionScope,
            Organization.Status status) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.capabilities = new ArrayList<>(capabilities);
        this.regionScope = new ArrayList<>(regionScope);
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getParentId() {
        return parentId;
    }

    public List<OrgCapability> getCapabilities() {
        return List.copyOf(capabilities);
    }

    public List<String> getRegionScope() {
        return List.copyOf(regionScope);
    }

    public Organization.Status getStatus() {
        return status;
    }
}
