package com.evolutionary.settlement.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "profit_sharing_rules")
public class ProfitSharingRuleJpaEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String orgId;

    /** 逗号分隔的 orgId:percentage 列表。 */
    @Column(nullable = false)
    private String splitsCsv;

    @Column(nullable = false)
    private int promoterBonusPercent;

    @Column(nullable = false)
    private Instant effectiveFrom;

    private Instant effectiveUntil;

    @Column(nullable = false)
    private int version;

    protected ProfitSharingRuleJpaEntity() {}

    public ProfitSharingRuleJpaEntity(
            String id,
            String orgId,
            String splitsCsv,
            int promoterBonusPercent,
            Instant effectiveFrom,
            Instant effectiveUntil,
            int version) {
        this.id = id;
        this.orgId = orgId;
        this.splitsCsv = splitsCsv;
        this.promoterBonusPercent = promoterBonusPercent;
        this.effectiveFrom = effectiveFrom;
        this.effectiveUntil = effectiveUntil;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getSplitsCsv() {
        return splitsCsv;
    }

    public int getPromoterBonusPercent() {
        return promoterBonusPercent;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public Instant getEffectiveUntil() {
        return effectiveUntil;
    }

    public int getVersion() {
        return version;
    }
}
