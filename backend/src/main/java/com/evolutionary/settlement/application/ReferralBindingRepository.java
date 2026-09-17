package com.evolutionary.settlement.application;

import com.evolutionary.settlement.domain.ReferralBinding;
import java.util.Optional;

/** 推广绑定仓储。 */
public interface ReferralBindingRepository {

    void save(ReferralBinding binding);

    Optional<ReferralBinding> findActiveByUserId(String userId);
}
