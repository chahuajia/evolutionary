package com.evolutionary.credit.infrastructure;

import com.evolutionary.credit.application.CreditPolicyRepository;
import com.evolutionary.credit.domain.CreditPolicy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内信用政策仓储（单测用；生产由 {@link JpaCreditPolicyRepository} 接管）。 */
public final class InMemoryCreditPolicyRepository implements CreditPolicyRepository {

    private final Map<Integer, CreditPolicy> byVersion = new ConcurrentHashMap<>();

    @Override
    public void save(CreditPolicy policy) {
        byVersion.put(policy.version(), policy);
    }

    @Override
    public Optional<CreditPolicy> findByVersion(int version) {
        return Optional.ofNullable(byVersion.get(version));
    }
}
