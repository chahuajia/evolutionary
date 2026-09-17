package com.evolutionary.credit.infrastructure;

import com.evolutionary.credit.application.CreditProfileRepository;
import com.evolutionary.credit.domain.CreditProfile;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCreditProfileRepository implements CreditProfileRepository {

    private final Map<String, CreditProfile> byUser = new ConcurrentHashMap<>();

    @Override
    public Optional<CreditProfile> findByUserId(String userId) {
        return Optional.ofNullable(byUser.get(userId));
    }

    @Override
    public void save(CreditProfile profile) {
        byUser.put(profile.userId(), profile);
    }
}
