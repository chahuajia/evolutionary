package com.evolutionary.settlement.application;

import com.evolutionary.settlement.domain.ProfitShareAccrual;
import java.util.List;

/** 分润意向仓储。 */
public interface ProfitShareAccrualRepository {

    void saveAll(List<ProfitShareAccrual> accruals);

    List<ProfitShareAccrual> findByOrderId(String orderId);

    List<ProfitShareAccrual> findAll();
}
