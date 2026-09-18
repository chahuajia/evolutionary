package com.evolutionary.operator.interfaces;

import com.evolutionary.mall.application.MerchantProfileRepository;
import com.evolutionary.mall.infrastructure.InMemoryMerchantProfileRepository;
import com.evolutionary.operator.application.ActivatePackageOverride;
import com.evolutionary.operator.application.ApproveOperatorDownline;
import com.evolutionary.operator.application.AuditLogRepository;
import com.evolutionary.operator.application.OnboardingApplicationRepository;
import com.evolutionary.operator.application.OrganizationRepository;
import com.evolutionary.operator.application.PackageOverrideRepository;
import com.evolutionary.operator.application.PackageTemplateRepository;
import com.evolutionary.operator.application.PublishPackageTemplate;
import com.evolutionary.operator.application.ResolveEffectiveProduct;
import com.evolutionary.operator.application.RevokePackageOverride;
import com.evolutionary.operator.domain.OnboardingApplication;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.OrgAuthorization;
import com.evolutionary.operator.domain.OrgCapability;
import com.evolutionary.operator.domain.OrgStatus;
import com.evolutionary.operator.domain.Organization;
import com.evolutionary.operator.domain.OverridableField;
import com.evolutionary.operator.domain.PackageTemplate;
import com.evolutionary.operator.domain.TemplateBaseProduct;
import com.evolutionary.operator.infrastructure.InMemoryAuditLogRepository;
import com.evolutionary.operator.infrastructure.InMemoryOnboardingApplicationRepository;
import com.evolutionary.operator.infrastructure.InMemoryOrganizationRepository;
import com.evolutionary.operator.infrastructure.InMemoryPackageOverrideRepository;
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

    /**
     * 组织须在 bean 构造时同步入仓，供 {@link #orgAuthorization} index（ApplicationRunner
     * 晚于 bean 创建）。
     */
    @Bean
    OrganizationRepository organizationRepository() {
        InMemoryOrganizationRepository organizations = new InMemoryOrganizationRepository();
        organizations.save(
                Organization.create(
                        "ORG-NEW",
                        "新商家待入驻",
                        null,
                        List.of(),
                        List.of("SZ"),
                        OrgStatus.ACTIVE));
        organizations.save(
                Organization.create(
                        "ORG-OP-PENDING",
                        "运营商待入驻",
                        null,
                        List.of(),
                        List.of("SZ"),
                        OrgStatus.ACTIVE));
        organizations.save(Organization.createRoot("ORG-L1", "华南", List.of("GD", "SZ")));
        organizations.save(Organization.createChild("ORG-L2", "深圳", "ORG-L1", List.of("SZ")));
        organizations.save(
                Organization.create(
                        "ORG-DL1",
                        "下线运营商待入驻",
                        "ORG-L1",
                        List.of(),
                        List.of("SZ"),
                        OrgStatus.ACTIVE));
        return organizations;
    }

    @Bean
    PackageTemplateRepository packageTemplateRepository() {
        return new InMemoryPackageTemplateRepository();
    }

    @Bean
    PackageOverrideRepository packageOverrideRepository() {
        return new InMemoryPackageOverrideRepository();
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
    OrgAuthorization orgAuthorization(OrganizationRepository organizations) {
        List<Organization> all = organizations.findAll();
        return new OrgAuthorization(OrgAuthorization.index(all.toArray(Organization[]::new)));
    }

    @Bean
    PublishPackageTemplate publishPackageTemplate(
            PackageTemplateRepository templates,
            AuditLogRepository auditLogs,
            OrganizationRepository organizations) {
        return new PublishPackageTemplate(
                templates, auditLogs, organizations, Clock.systemUTC());
    }

    @Bean
    ActivatePackageOverride activatePackageOverride(
            PackageTemplateRepository templates,
            PackageOverrideRepository overrides,
            AuditLogRepository auditLogs,
            OrgAuthorization orgAuthorization) {
        return new ActivatePackageOverride(
                templates, overrides, auditLogs, orgAuthorization, Clock.systemUTC());
    }

    @Bean
    RevokePackageOverride revokePackageOverride(
            PackageOverrideRepository overrides, AuditLogRepository auditLogs) {
        return new RevokePackageOverride(overrides, auditLogs, Clock.systemUTC());
    }

    @Bean
    ResolveEffectiveProduct resolveEffectiveProduct(
            PackageTemplateRepository templates, PackageOverrideRepository overrides) {
        return new ResolveEffectiveProduct(templates, overrides);
    }

    @Bean
    ApproveOperatorDownline approveOperatorDownline(
            OnboardingApplicationRepository applications, OrganizationRepository organizations) {
        return new ApproveOperatorDownline(applications, organizations, Clock.systemUTC());
    }

    /**
     * 正式本地种子：APP-M1（MERCHANT · SUBMITTED · ORG-NEW）；APP-OP1（OPERATOR · 总后台负例）；
     * APP-DL1（OPERATOR · SUBMITTED · ORG-DL1 · ORG-L1 下线正例 · 切片29a）；
     * ORG-L1 + ORG-L2（深圳，已在 organizationRepository 入仓）+ T-DRAFT-1（AC-24）+ T-PUB-1（已发布 ·
     * AC-26）；T-DRAFT-M（禁发负例）。
     */
    @Bean
    ApplicationRunner seedOperator(
            OnboardingApplicationRepository applications,
            PackageTemplateRepository templates) {
        return args -> {
            Instant submittedAt = Instant.parse("2026-09-17T06:00:00Z");
            Instant publishedAt = Instant.parse("2026-09-17T07:00:00Z");

            applications.save(
                    OnboardingApplication.submit(
                            "APP-M1", "ORG-NEW", OrgCapability.MERCHANT, submittedAt));
            applications.save(
                    OnboardingApplication.submit(
                            "APP-OP1",
                            "ORG-OP-PENDING",
                            OrgCapability.OPERATOR,
                            submittedAt));
            applications.save(
                    OnboardingApplication.submit(
                            "APP-DL1", "ORG-DL1", OrgCapability.OPERATOR, submittedAt));

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

            PackageTemplate pubDraft =
                    PackageTemplate.createDraft(
                            "T-PUB-1",
                            "ORG-L1",
                            base,
                            List.of(OverridableField.PRICE, OverridableField.DISPLAY_NAME));
            OperatorOutcome<PackageTemplate> published = pubDraft.publish(publishedAt);
            templates.save(((OperatorOutcome.Ok<PackageTemplate>) published).value());
        };
    }
}
