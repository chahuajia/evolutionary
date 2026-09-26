package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Product;

public interface ProductRepository {
    Product get(String productId);

    void save(Product product);
}