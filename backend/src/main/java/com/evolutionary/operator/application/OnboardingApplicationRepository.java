package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.OnboardingApplication;
import java.util.Optional;

/** 入驻申请仓储端口。 */
public interface OnboardingApplicationRepository {

    void save(OnboardingApplication application);

    Optional<OnboardingApplication> findById(String id);

    default OnboardingApplication get(String id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("未知入驻申请: " + id));
    }
}
