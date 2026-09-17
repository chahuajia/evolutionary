package com.evolutionary.settlement.application;

import com.evolutionary.settlement.domain.SettlementBatch;

/** 结算批仓储。 */
public interface SettlementBatchRepository {

    void save(SettlementBatch batch);

    SettlementBatch get(String batchId);
}
