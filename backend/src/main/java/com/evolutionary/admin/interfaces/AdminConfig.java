package com.evolutionary.admin.interfaces;

import com.evolutionary.mall.application.MerchantProfileRepository;
import com.evolutionary.operator.application.ApproveMerchantOnboarding;
import com.evolutionary.operator.application.AuditLogRepository;
import com.evolutionary.operator.application.OnboardingApplicationRepository;
import com.evolutionary.operator.application.OrganizationRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 平台总后台装配（切片26a：商家入驻批准用例注入；切片30b：审计；种子仍在 {@code OperatorConfig}）。
 */
@Configuration
public class AdminConfig {

    @Bean
    ApproveMerchantOnboarding approveMerchantOnboarding(
            OnboardingApplicationRepository applications,
            OrganizationRepository organizations,
            MerchantProfileRepository merchants,
            AuditLogRepository auditLogs) {
        return new ApproveMerchantOnboarding(
                applications, organizations, merchants, auditLogs, Clock.systemUTC());
    }
}
