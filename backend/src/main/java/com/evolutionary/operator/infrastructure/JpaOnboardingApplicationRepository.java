package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.application.OnboardingApplicationRepository;
import com.evolutionary.operator.domain.OnboardingApplication;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 入驻申请 JPA 适配（表 onboarding_applications）。 */
@Component
public final class JpaOnboardingApplicationRepository implements OnboardingApplicationRepository {

    private final OnboardingApplicationJpaRepository jpa;

    public JpaOnboardingApplicationRepository(OnboardingApplicationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(OnboardingApplication application) {
        jpa.save(
                new OnboardingApplicationJpaEntity(
                        application.id(),
                        application.orgId(),
                        application.capability(),
                        application.status(),
                        application.submittedAt(),
                        application.reviewedAt()));
    }

    @Override
    public Optional<OnboardingApplication> findById(String id) {
        return jpa.findById(id).map(JpaOnboardingApplicationRepository::toDomain);
    }

    private static OnboardingApplication toDomain(OnboardingApplicationJpaEntity row) {
        return OnboardingApplication.rehydrate(
                row.getId(),
                row.getOrgId(),
                row.getCapability(),
                row.getStatus(),
                row.getSubmittedAt(),
                row.getReviewedAt());
    }
}
