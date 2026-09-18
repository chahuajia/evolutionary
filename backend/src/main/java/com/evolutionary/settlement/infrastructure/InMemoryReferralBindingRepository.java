package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.application.ReferralBindingRepository;
import com.evolutionary.settlement.domain.ReferralBinding;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内推广绑定仓储。 */
public final class InMemoryReferralBindingRepository implements ReferralBindingRepository {

    private final Map<String, ReferralBinding> byUser = new ConcurrentHashMap<>();

    @Override
    public void save(ReferralBinding binding) {
        byUser.put(binding.userId(), binding);
    }

    @Override
    public Optional<ReferralBinding> findActiveByUserId(String userId) {
        return Optional.ofNullable(byUser.get(userId));
    }
}
