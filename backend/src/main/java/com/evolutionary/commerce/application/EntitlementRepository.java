package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Entitlement;

public interface EntitlementRepository {
    void save(Entitlement entitlement);

    Entitlement get(String entitlementId);
}
