package com.evolutionary.mall.infrastructure;

import com.evolutionary.mall.application.CampaignRepository;
import com.evolutionary.mall.domain.Campaign;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内营销活动仓储（单测用；生产由 {@link JpaCampaignRepository} 接管）。 */
public final class InMemoryCampaignRepository implements CampaignRepository {

    private final Map<String, Campaign> byId = new ConcurrentHashMap<>();

    @Override
    public Optional<Campaign> findById(String campaignId) {
        return Optional.ofNullable(byId.get(campaignId));
    }

    @Override
    public void save(Campaign campaign) {
        byId.put(campaign.id(), campaign);
    }
}
