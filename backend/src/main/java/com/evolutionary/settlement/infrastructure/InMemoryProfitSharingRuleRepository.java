package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.application.ProfitSharingRuleRepository;
import com.evolutionary.settlement.domain.ProfitSharingRule;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内分润规则仓储（单测用；生产由 {@link JpaProfitSharingRuleRepository} 接管）。 */
public final class InMemoryProfitSharingRuleRepository implements ProfitSharingRuleRepository {

    private final Map<String, ProfitSharingRule> byOrg = new ConcurrentHashMap<>();

    @Override
    public void save(ProfitSharingRule rule) {
        byOrg.put(rule.orgId(), rule);
    }

    @Override
    public Optional<ProfitSharingRule> findByOrgId(String orgId) {
        return Optional.ofNullable(byOrg.get(orgId));
    }
}
