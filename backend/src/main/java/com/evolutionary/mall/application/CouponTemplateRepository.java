package com.evolutionary.mall.application;

import com.evolutionary.mall.domain.CouponTemplate;
import java.util.Optional;

public interface CouponTemplateRepository {
    Optional<CouponTemplate> findById(String templateId);

    void save(CouponTemplate template);
}
