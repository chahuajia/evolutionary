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
     * 某组织的全部意向（**读侧**：供工作台列表）。
     *
     * <p>与 {@link #findPendingCreatedBetween} 的区别是**用途**：
     * 那条服务结算批次（只要 PENDING）；这条服务**展示** ——
     * 已结算/已冲销的也必须在列表里，否则用户看不到它们，
     * 也就无从知道"为什么这条不能动"（反面：列表只有 PENDING，
     * 界面上永远没有需要解释的东西，展示不变量就成了死代码）。
     */
    List<ProfitShareAccrual> findByOrgId(String orgId);

    /**
     * 周期内 PENDING 意向（含 periodStart，不含 periodEnd）。
     *
     * <p>REVERSED / SETTLED 自然排除 → INV-15。
     */
    List<ProfitShareAccrual> findPendingCreatedBetween(Instant periodStart, Instant periodEnd);

    List<ProfitShareAccrual> findAll();
}
