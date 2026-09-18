package com.evolutionary.mall.infrastructure;

import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.application.CouponTemplateRepository;
import com.evolutionary.mall.domain.CouponTemplate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 优惠券模板 JPA 适配。 */
@Component
public final class JpaCouponTemplateRepository implements CouponTemplateRepository {

    private final CouponTemplateJpaRepository jpa;

    public JpaCouponTemplateRepository(CouponTemplateJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<CouponTemplate> findById(String templateId) {
        return jpa.findById(templateId).map(JpaCouponTemplateRepository::toDomain);
    }

    @Override
    public void save(CouponTemplate template) {
        jpa.save(
                new CouponTemplateJpaEntity(
                        template.id(),
                        template.issuerOrgId(),
                        template.issuerType(),
                        template.kind(),
                        template.value(),
                        template.minSpend().map(Money::cents).orElse(null),
                        template.minSpend().map(m -> m.currency().name()).orElse(null),
                        template.scope(),
                        toCsv(template.scopeIds()),
                        template.mutexGroup(),
                        template.campaignId(),
                        template.validFrom(),
                        template.validUntil(),
                        template.perUserLimit()));
    }

    private static CouponTemplate toDomain(CouponTemplateJpaEntity row) {
        Money minSpend =
                row.getMinSpendCents() == null
                        ? null
                        : new Money(row.getMinSpendCents(), Currency.valueOf(row.getMinSpendCurrency()));
        return CouponTemplate.rehydrate(
                row.getId(),
                row.getIssuerOrgId(),
                row.getIssuerType(),
                row.getKind(),
                row.getValueAmount(),
                minSpend,
                row.getScope(),
                fromCsv(row.getScopeIdsCsv()),
                row.getMutexGroup(),
                row.getCampaignId(),
                row.getValidFrom(),
                row.getValidUntil(),
                row.getPerUserLimit());
    }

    private static String toCsv(List<String> ids) {
        return String.join(",", ids);
    }

    private static List<String> fromCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return List.copyOf(Arrays.asList(csv.split(",")));
    }
}
