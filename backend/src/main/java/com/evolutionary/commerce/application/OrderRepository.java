package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Order;

public interface OrderRepository {
    void save(Order order);
}
