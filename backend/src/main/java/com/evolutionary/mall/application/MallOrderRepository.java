package com.evolutionary.mall.application;

import com.evolutionary.mall.domain.MallOrder;
import java.util.Optional;

/** 商城订单仓储端口。 */
public interface MallOrderRepository {
    void save(MallOrder order);

    Optional<MallOrder> findById(String orderId);
}
