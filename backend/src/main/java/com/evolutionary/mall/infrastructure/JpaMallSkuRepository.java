package com.evolutionary.mall.infrastructure;

import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.application.MallSkuRepository;
import com.evolutionary.mall.domain.MallSku;
import org.springframework.stereotype.Component;

/** 商城 SKU JPA 适配。 */
@Component
public final class JpaMallSkuRepository implements MallSkuRepository {

    private final MallSkuJpaRepository jpa;

    public JpaMallSkuRepository(MallSkuJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public MallSku get(String skuId) {
        return jpa.findById(skuId)
                .map(JpaMallSkuRepository::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("unknown sku"));
    }

    @Override
    public void save(MallSku sku) {
        jpa.save(
                new MallSkuJpaEntity(
                        sku.id(),
                        sku.merchantOrgId(),
                        sku.name(),
                        sku.price().cents(),
                        sku.price().currency().name(),
                        sku.stock(),
                        sku.status()));
    }

    private static MallSku toDomain(MallSkuJpaEntity row) {
        Money price = new Money(row.getPriceCents(), Currency.valueOf(row.getPriceCurrency()));
        return MallSku.rehydrate(
                row.getId(),
                row.getMerchantOrgId(),
                row.getName(),
                price,
                row.getStock(),
                row.getStatus());
    }
}
