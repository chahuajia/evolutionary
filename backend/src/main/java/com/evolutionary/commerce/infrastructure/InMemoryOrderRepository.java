package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.OrderRepository;
import com.evolutionary.commerce.domain.Order;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内订单仓（信用购 HTTP 等）。 */
public final class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> byId = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        byId.put(order.id(), order);
    }

    @Override
    public Order get(String orderId) {
        Order order = byId.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("order not found: " + orderId);
        }
        return order;
    }
}
