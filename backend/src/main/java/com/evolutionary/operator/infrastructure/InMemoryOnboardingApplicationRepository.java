package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.application.OnboardingApplicationRepository;
import com.evolutionary.operator.domain.OnboardingApplication;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内入驻申请仓储。 */
public final class InMemoryOnboardingApplicationRepository
        implements OnboardingApplicationRepository {

    private final Map<String, OnboardingApplication> byId = new ConcurrentHashMap<>();

    @Override
    public void save(OnboardingApplication application) {
        byId.put(application.id(), application);
    }

    @Override
    public Optional<OnboardingApplication> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }
}
