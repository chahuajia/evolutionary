package com.evolutionary.mall.application;

import com.evolutionary.mall.domain.MallSku;

/** 商城 SKU 仓储端口。 */
public interface MallSkuRepository {
    MallSku get(String skuId);

    void save(MallSku sku);
}
