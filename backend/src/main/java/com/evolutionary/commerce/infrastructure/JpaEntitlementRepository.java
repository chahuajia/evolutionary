package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.EntitlementStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 权益 JPA 适配。 */
@Component
public final class JpaEntitlementRepository implements EntitlementRepository {

    private final EntitlementJpaRepository jpa;

    public JpaEntitlementRepository(EntitlementJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Entitlement entitlement) {
        jpa.save(
                new EntitlementJpaEntity(
                        entitlement.id(),
                        entitlement.orderId(),
                        entitlement.userId(),
                        entitlement.productId(),
                        entitlement.validFrom(),
                        entitlement.validUntil(),
                        entitlement.status(),
                        entitlement.remainingSwaps(),
                        entitlement.meteringMode()));
    }

    @Override
    public Entitlement get(String entitlementId) {
        return jpa.findById(entitlementId)
                .map(JpaEntitlementRepository::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("unknown entitlement"));
    }

    @Override
    public Optional<Entitlement> findByOrderId(String orderId) {
        return jpa.findByOrderId(orderId).map(JpaEntitlementRepository::toDomain);
    }

    @Override
    public List<Entitlement> findActiveByUser(String userId) {
        return findByUserIdAndStatus(userId, EntitlementStatus.ACTIVE);
    }

    @Override
    public List<Entitlement> findByUserIdAndStatus(String userId, EntitlementStatus status) {
        List<Entitlement> result = new ArrayList<>();
        for (EntitlementJpaEntity row : jpa.findByUserIdAndStatus(userId, status)) {
            result.add(toDomain(row));
        }
        return result;
    }

    private static Entitlement toDomain(EntitlementJpaEntity row) {
        return Entitlement.rehydrate(
                row.getId(),
                row.getOrderId(),
                row.getUserId(),
                row.getProductId(),
                row.getValidFrom(),
                row.getValidUntil(),
                row.getStatus(),
                row.getRemainingSwaps(),
                row.getMeteringMode());
    }
}
