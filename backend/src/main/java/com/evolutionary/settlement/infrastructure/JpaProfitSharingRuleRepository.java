package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.application.ProfitSharingRuleRepository;
import com.evolutionary.settlement.domain.ProfitSharingRule;
import com.evolutionary.settlement.domain.ProfitSplit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 分润规则 JPA 适配。 */
@Component
public final class JpaProfitSharingRuleRepository implements ProfitSharingRuleRepository {

    private final ProfitSharingRuleJpaRepository jpa;

    public JpaProfitSharingRuleRepository(ProfitSharingRuleJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(ProfitSharingRule rule) {
        jpa.save(
                new ProfitSharingRuleJpaEntity(
                        rule.id(),
                        rule.orgId(),
                        toCsv(rule.splits()),
                        rule.promoterBonusPercent(),
                        rule.effectiveFrom(),
                        rule.effectiveUntil(),
                        rule.version()));
    }

    @Override
    public Optional<ProfitSharingRule> findByOrgId(String orgId) {
        return jpa.findByOrgId(orgId).map(JpaProfitSharingRuleRepository::toDomain);
    }

    private static ProfitSharingRule toDomain(ProfitSharingRuleJpaEntity row) {
        return ProfitSharingRule.rehydrate(
                row.getId(),
                row.getOrgId(),
                fromCsv(row.getSplitsCsv()),
                row.getPromoterBonusPercent(),
                row.getEffectiveFrom(),
                row.getEffectiveUntil(),
                row.getVersion());
    }

    private static String toCsv(List<ProfitSplit> splits) {
        return String.join(
                ",",
                splits.stream().map(s -> s.orgId() + ":" + s.percentage()).toList());
    }

    private static List<ProfitSplit> fromCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return List.copyOf(
                Arrays.stream(csv.split(","))
                        .map(
                                part -> {
                                    String[] kv = part.split(":", 2);
                                    return ProfitSplit.of(kv[0], Integer.parseInt(kv[1]));
                                })
                        .toList());
    }
}
