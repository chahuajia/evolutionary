package com.evolutionary.operator.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data 仓储（表 onboarding_applications）。 */
public interface OnboardingApplicationJpaRepository
        extends JpaRepository<OnboardingApplicationJpaEntity, String> {}
