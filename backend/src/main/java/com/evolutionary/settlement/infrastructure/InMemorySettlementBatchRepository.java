package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.application.SettlementBatchRepository;
import com.evolutionary.settlement.domain.SettlementBatch;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内结算批仓储。 */
public final class InMemorySettlementBatchRepository implements SettlementBatchRepository {

    private final Map<String, SettlementBatch> byId = new ConcurrentHashMap<>();

    @Override
    public void save(SettlementBatch batch) {
        byId.put(batch.id(), batch);
    }

    @Override
    public SettlementBatch get(String batchId) {
        SettlementBatch b = byId.get(batchId);
        if (b == null) {
            throw new IllegalArgumentException("unknown settlement batch: " + batchId);
        }
        return b;
    }
}
