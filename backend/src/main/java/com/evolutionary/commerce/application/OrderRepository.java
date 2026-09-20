package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Order;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);

    Order get(String orderId);

    /**
     * 只读查找。默认用 {@link #get} 吞掉未知 id，避免各测试替身再实现一遍。
     */
    default Optional<Order> findById(String orderId) {
        try {
            return Optional.of(get(orderId));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
