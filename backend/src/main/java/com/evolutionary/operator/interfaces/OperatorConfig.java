package com.evolutionary.operator.interfaces;

import com.evolutionary.mall.application.MerchantProfileRepository;
import com.evolutionary.mall.infrastructure.InMemoryMerchantProfileRepository;
import com.evolutionary.operator.application.ApproveMerchantOnboarding;
import com.evolutionary.operator.application.AuditLogRepository;
import com.evolutionary.operator.application.OnboardingApplicationRepository;
import com.evolutionary.operator.application.OrganizationRepository;
import com.evolutionary.operator.application.PackageTemplateRepository;
import com.evolutionary.operator.application.PublishPackageTemplate;
import com.evolutionary.operator.domain.OnboardingApplication;
import com.evolutionary.operator.domain.OrgCapability;
import com.evolutionary.operator.domain.OrgStatus;
import com.evolutionary.operator.domain.Organization;
import com.evolutionary.operator.domain.OverridableField;
import com.evolutionary.operator.domain.PackageTemplate;
import com.evolutionary.operator.domain.TemplateBaseProduct;
import com.evolutionary.operator.infrastructure.InMemoryAuditLogRepository;
import com.evolutionary.operator.infrastructure.InMemoryOnboardingApplicationRepository;
import com.evolutionary.operator.infrastructure.InMemoryOrganizationRepository;
import com.evolutionary.operator.infrastructure.InMemoryPackageTemplateRepository;
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
    PackageTemplateRepository packageTemplateRepository() {
        return new InMemoryPackageTemplateRepository();
    }

    @Bean
    AuditLogRepository auditLogRepository() {
        return new InMemoryAuditLogRepository();
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

    @Bean
    PublishPackageTemplate publishPackageTemplate(
            PackageTemplateRepository templates,
            AuditLogRepository auditLogs,
            OrganizationRepository organizations) {
        return new PublishPackageTemplate(
                templates, auditLogs, organizations, Clock.systemUTC());
    }

    /**
     * 正式本地种子：APP-M1（MERCHANT · SUBMITTED · ORG-NEW）；APP-OP1（OPERATOR · 负例）；
     * ORG-L1 + T-DRAFT-1（AC-24 发布正例）；T-DRAFT-M（ORG-NEW · 禁发负例）。
     */
    @Bean
    ApplicationRunner seedOperator(
            OnboardingApplicationRepository applications,
            OrganizationRepository organizations,
            PackageTemplateRepository templates) {
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

            organizations.save(Organization.createRoot("ORG-L1", "华南", List.of("GD", "SZ")));
            TemplateBaseProduct base = TemplateBaseProduct.of("30天卡", 3000, 30);
            templates.save(
                    PackageTemplate.createDraft(
                            "T-DRAFT-1",
                            "ORG-L1",
                            base,
                            List.of(OverridableField.PRICE, OverridableField.DISPLAY_NAME)));
            templates.save(
                    PackageTemplate.createDraft(
                            "T-DRAFT-M",
                            "ORG-NEW",
                            base,
                            List.of(OverridableField.PRICE)));
        };
    }
}
