package com.evolutionary.operator.interfaces;

import com.evolutionary.mall.application.MerchantProfileRepository;
import com.evolutionary.mall.infrastructure.InMemoryMerchantProfileRepository;
import com.evolutionary.operator.application.ApproveMerchantOnboarding;
import com.evolutionary.operator.application.OnboardingApplicationRepository;
import com.evolutionary.operator.application.OrganizationRepository;
import com.evolutionary.operator.domain.OnboardingApplication;
import com.evolutionary.operator.domain.OrgCapability;
import com.evolutionary.operator.domain.OrgStatus;
import com.evolutionary.operator.domain.Organization;
import com.evolutionary.operator.infrastructure.InMemoryOnboardingApplicationRepository;
import com.evolutionary.operator.infrastructure.InMemoryOrganizationRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OperatorConfig {

    @Bean
    OnboardingApplicationRepository onboardingApplicationRepository() {
        return new InMemoryOnboardingApplicationRepository();
    }

    @Bean
    OrganizationRepository organizationRepository() {
        return new InMemoryOrganizationRepository();
    }

    @Bean
    MerchantProfileRepository merchantProfileRepository() {
        return new InMemoryMerchantProfileRepository();
    }

    @Bean
    ApproveMerchantOnboarding approveMerchantOnboarding(
            OnboardingApplicationRepository applications,
            OrganizationRepository organizations,
            MerchantProfileRepository merchants) {
        return new ApproveMerchantOnboarding(
                applications, organizations, merchants, Clock.systemUTC());
    }

    /**
     * 正式本地种子：APP-M1（MERCHANT · SUBMITTED · ORG-NEW）；APP-OP1（OPERATOR · 负例）。
     */
    @Bean
    ApplicationRunner seedOperator(
            OnboardingApplicationRepository applications, OrganizationRepository organizations) {
        return args -> {
            Instant submittedAt = Instant.parse("2026-09-17T06:00:00Z");

            organizations.save(
                    Organization.create(
                            "ORG-NEW",
                            "新商家待入驻",
                            null,
                            List.of(),
                            List.of("SZ"),
                            OrgStatus.ACTIVE));
            applications.save(
                    OnboardingApplication.submit(
                            "APP-M1", "ORG-NEW", OrgCapability.MERCHANT, submittedAt));

            organizations.save(
                    Organization.create(
                            "ORG-OP-PENDING",
                            "运营商待入驻",
                            null,
                            List.of(),
                            List.of("SZ"),
                            OrgStatus.ACTIVE));
            applications.save(
                    OnboardingApplication.submit(
                            "APP-OP1",
                            "ORG-OP-PENDING",
                            OrgCapability.OPERATOR,
                            submittedAt));
        };
    }
}
