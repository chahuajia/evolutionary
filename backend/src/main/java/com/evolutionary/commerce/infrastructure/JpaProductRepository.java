package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.ProductRepository;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.commerce.domain.SwapLimit;
import org.springframework.stereotype.Component;

/** 商品 JPA 适配。 */
@Component
public final class JpaProductRepository implements ProductRepository {

    private final ProductJpaRepository jpa;

    public JpaProductRepository(ProductJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Product get(String productId) {
        return jpa.findById(productId)
                .map(JpaProductRepository::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("unknown product"));
    }

    @Override
    public void save(Product product) {
        Long meteredCents = null;
        String meteredCurrency = null;
        if (product.meteredRate() != null) {
            meteredCents = product.meteredRate().cents();
            meteredCurrency = product.meteredRate().currency().name();
        }
        jpa.save(
                new ProductJpaEntity(
                        product.id(),
                        product.orgId(),
                        product.name(),
                        product.price().cents(),
                        product.price().currency().name(),
                        product.durationDays(),
                        product.swapLimit().finiteOrNull(),
                        product.status(),
                        meteredCents,
                        meteredCurrency));
    }

    private static Product toDomain(ProductJpaEntity row) {
        Money price = new Money(row.getPriceCents(), Currency.valueOf(row.getPriceCurrency()));
        SwapLimit limit =
                row.getSwapLimitFinite() == null
                        ? SwapLimit.unlimited()
                        : SwapLimit.finite(row.getSwapLimitFinite());
        Money metered = null;
        if (row.getMeteredRateCents() != null && row.getMeteredRateCurrency() != null) {
            metered =
                    new Money(
                            row.getMeteredRateCents(),
                            Currency.valueOf(row.getMeteredRateCurrency()));
        }
        return Product.rehydrate(
                row.getId(),
                row.getOrgId(),
                row.getName(),
                price,
                row.getDurationDays(),
                limit,
                row.getStatus(),
                metered);
    }
}
