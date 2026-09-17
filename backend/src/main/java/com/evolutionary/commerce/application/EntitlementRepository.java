package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Entitlement;
import java.util.Optional;

public interface EntitlementRepository {
    void save(Entitlement entitlement);

    Entitlement get(String entitlementId);

    Optional<Entitlement> findByOrderId(String orderId);
}
