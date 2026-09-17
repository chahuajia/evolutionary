package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.EntitlementStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内权益仓储（非计量换电路径最小实现）。 */
public final class InMemoryEntitlementRepository implements EntitlementRepository {

    private final Map<String, Entitlement> byId = new ConcurrentHashMap<>();

    @Override
    public void save(Entitlement entitlement) {
        byId.put(entitlement.id(), entitlement);
    }

    @Override
    public Entitlement get(String entitlementId) {
        Entitlement e = byId.get(entitlementId);
        if (e == null) {
            throw new IllegalArgumentException("unknown entitlement");
        }
        return e;
    }

    @Override
    public Optional<Entitlement> findByOrderId(String orderId) {
        return byId.values().stream().filter(e -> e.orderId().equals(orderId)).findFirst();
    }

    @Override
    public List<Entitlement> findActiveByUser(String userId) {
        return byId.values().stream()
                .filter(e -> e.userId().equals(userId))
                .filter(e -> e.status() == EntitlementStatus.ACTIVE)
                .toList();
    }
}
