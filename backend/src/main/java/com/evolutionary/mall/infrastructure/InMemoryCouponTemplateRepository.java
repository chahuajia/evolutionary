package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.application.CouponTemplateRepository;
import com.evolutionary.mall.domain.CouponTemplate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCouponTemplateRepository implements CouponTemplateRepository {

    private final Map<String, CouponTemplate> byId = new ConcurrentHashMap<>();

    @Override
    public Optional<CouponTemplate> findById(String templateId) {
        return Optional.ofNullable(byId.get(templateId));
    }

    @Override
    public void save(CouponTemplate template) {
        byId.put(template.id(), template);
    }
}
