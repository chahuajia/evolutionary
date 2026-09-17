package com.evolutionary.mall.application;

import com.evolutionary.mall.domain.Campaign;
import java.util.Optional;

public interface CampaignRepository {
    Optional<Campaign> findById(String campaignId);

    void save(Campaign campaign);
}
