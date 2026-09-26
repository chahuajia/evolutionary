package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Entitlement;
import java.util.List;
import java.util.Optional;

public interface EntitlementRepository {
    void save(Entitlement entitlement);

    Entitlement get(String entitlementId);

    /**
     * 只读查找。默认用 {@link #get} 吞掉未知 id，避免各测试替身再实现一遍。
     */
    default Optional<Entitlement> findById(String entitlementId) {
        try {
            return Optional.of(get(entitlementId));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    Optional<Entitlement> findByOrderId(String orderId);

    /** 用户当前 ACTIVE 权益（不含时间/次数可用性过滤）。 */
    List<Entitlement> findActiveByUser(String userId);

    /**
     * 按状态查用户权益。默认：ACTIVE 委托 {@link #findActiveByUser}；其他返回空（测试可覆盖）。
     */
    default List<Entitlement> findByUserIdAndStatus(String userId, Entitlement.Status status) {
        if (status == Entitlement.Status.ACTIVE) {
            return findActiveByUser(userId);
        }
        return List.of();
    }
}
