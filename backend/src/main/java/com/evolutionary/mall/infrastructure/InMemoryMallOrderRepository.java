package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.application.MallOrderRepository;
import com.evolutionary.mall.domain.MallOrder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内商城订单仓储。 */
public final class InMemoryMallOrderRepository implements MallOrderRepository {

    private final Map<String, MallOrder> byId = new ConcurrentHashMap<>();

    @Override
    public void save(MallOrder order) {
        byId.put(order.id(), order);
    }

    @Override
    public Optional<MallOrder> findById(String orderId) {
        return Optional.ofNullable(byId.get(orderId));
    }
}
