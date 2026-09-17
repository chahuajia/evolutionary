package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Entitlement;
import java.util.List;
import java.util.Optional;

public interface EntitlementRepository {
    void save(Entitlement entitlement);

    Entitlement get(String entitlementId);

    Optional<Entitlement> findByOrderId(String orderId);

    /** 用户当前 ACTIVE 权益（不含时间/次数可用性过滤）。 */
    List<Entitlement> findActiveByUser(String userId);
}
