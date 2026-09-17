package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.ProductRepository;
import com.evolutionary.commerce.domain.Product;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内商品仓储（计量换电路径）。 */
public final class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> byId = new ConcurrentHashMap<>();

    @Override
    public Product get(String productId) {
        Product p = byId.get(productId);
        if (p == null) {
            throw new IllegalArgumentException("unknown product");
        }
        return p;
    }

    public void save(Product product) {
        byId.put(product.id(), product);
    }
}
