package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.application.ProfitShareAccrualRepository;
import com.evolutionary.settlement.domain.AccrualStatus;
import com.evolutionary.settlement.domain.ProfitShareAccrual;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内分润意向仓储。 */
public final class InMemoryProfitShareAccrualRepository implements ProfitShareAccrualRepository {

    private final Map<String, ProfitShareAccrual> byId = new ConcurrentHashMap<>();

    @Override
    public void save(ProfitShareAccrual accrual) {
        byId.put(accrual.id(), accrual);
    }

    @Override
    public void saveAll(List<ProfitShareAccrual> accruals) {
        accruals.forEach(this::save);
    }

    @Override
    public List<ProfitShareAccrual> findByOrderId(String orderId) {
        return byId.values().stream().filter(a -> a.orderId().equals(orderId)).toList();
    }

    @Override
    public List<ProfitShareAccrual> findPendingCreatedBetween(
            Instant periodStart, Instant periodEnd) {
        return byId.values().stream()
                .filter(a -> a.status() == AccrualStatus.PENDING)
                .filter(
                        a ->
                                !a.createdAt().isBefore(periodStart)
                                        && a.createdAt().isBefore(periodEnd))
                .toList();
    }

    @Override
    public List<ProfitShareAccrual> findAll() {
        return List.copyOf(byId.values());
    }
}
