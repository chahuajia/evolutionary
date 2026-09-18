package com.evolutionary.mall.infrastructure;

import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.application.CampaignRepository;
import com.evolutionary.mall.domain.Campaign;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 营销活动 JPA 适配。 */
@Component
public final class JpaCampaignRepository implements CampaignRepository {

    private final CampaignJpaRepository jpa;

    public JpaCampaignRepository(CampaignJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Campaign> findById(String campaignId) {
        return jpa.findById(campaignId).map(JpaCampaignRepository::toDomain);
    }

    @Override
    public void save(Campaign campaign) {
        jpa.save(
                new CampaignJpaEntity(
                        campaign.id(),
                        campaign.ownerOrgId(),
                        campaign.name(),
                        campaign.budgetTotal().cents(),
                        campaign.budgetTotal().currency().name(),
                        campaign.budgetRemaining().cents(),
                        campaign.budgetRemaining().currency().name(),
                        campaign.status(),
                        toCsv(campaign.couponTemplateIds())));
    }

    private static Campaign toDomain(CampaignJpaEntity row) {
        Money budgetTotal =
                new Money(row.getBudgetTotalCents(), Currency.valueOf(row.getBudgetTotalCurrency()));
        Money budgetRemaining =
                new Money(
                        row.getBudgetRemainingCents(),
                        Currency.valueOf(row.getBudgetRemainingCurrency()));
        return Campaign.rehydrate(
                row.getId(),
                row.getOwnerOrgId(),
                row.getName(),
                budgetTotal,
                budgetRemaining,
                row.getStatus(),
                fromCsv(row.getCouponTemplateIdsCsv()));
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
