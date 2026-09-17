package com.evolutionary.settlement.application;

import com.evolutionary.settlement.domain.ProfitSharingRule;
import java.util.Optional;

/** 分润规则仓储。 */
public interface ProfitSharingRuleRepository {

    void save(ProfitSharingRule rule);

    /** 按售卖方 org 查找当前规则（切片2：单规则简化）。 */
    Optional<ProfitSharingRule> findByOrgId(String orgId);
}
