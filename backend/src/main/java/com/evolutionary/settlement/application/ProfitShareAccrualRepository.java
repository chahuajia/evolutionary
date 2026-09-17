package com.evolutionary.settlement.application;

import com.evolutionary.settlement.domain.ProfitShareAccrual;
import java.time.Instant;
import java.util.List;

/** 分润意向仓储。 */
public interface ProfitShareAccrualRepository {

    /** 按 id 覆盖写入（结算 / 冲销状态迁移）。 */
    void save(ProfitShareAccrual accrual);

    void saveAll(List<ProfitShareAccrual> accruals);

    List<ProfitShareAccrual> findByOrderId(String orderId);

    /**
     * 周期内 PENDING 意向（含 periodStart，不含 periodEnd）。
     *
     * <p>REVERSED / SETTLED 自然排除 → INV-15。
     */
    List<ProfitShareAccrual> findPendingCreatedBetween(Instant periodStart, Instant periodEnd);

    List<ProfitShareAccrual> findAll();
}
