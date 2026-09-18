package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.application.MallSkuRepository;
import com.evolutionary.mall.domain.MallSku;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内商城 SKU 仓储。 */
public final class InMemoryMallSkuRepository implements MallSkuRepository {

    private final Map<String, MallSku> byId = new ConcurrentHashMap<>();

    @Override
    public MallSku get(String skuId) {
        MallSku sku = byId.get(skuId);
        if (sku == null) {
            throw new IllegalArgumentException("unknown sku: " + skuId);
        }
        return sku;
    }

    @Override
    public void save(MallSku sku) {
        byId.put(sku.id(), sku);
    }
}
