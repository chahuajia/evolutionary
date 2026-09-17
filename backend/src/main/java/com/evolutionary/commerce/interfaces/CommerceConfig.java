package com.evolutionary.commerce.interfaces;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.BatteryAssetRepository;
import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.application.PerformEntitledSwap;
import com.evolutionary.commerce.application.UsageEventRepository;
import com.evolutionary.commerce.domain.BatteryAsset;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.EntitlementStatus;
import com.evolutionary.commerce.infrastructure.InMemoryAccountRepository;
import com.evolutionary.commerce.infrastructure.InMemoryBatteryAssetRepository;
import com.evolutionary.commerce.infrastructure.InMemoryEntitlementRepository;
import com.evolutionary.commerce.infrastructure.InMemoryLedgerRepository;
import com.evolutionary.commerce.infrastructure.InMemoryUsageEventRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommerceConfig {

    @Bean
    EntitlementRepository entitlementRepository() {
        return new InMemoryEntitlementRepository();
    }

    @Bean
    AccountRepository accountRepository() {
        return new InMemoryAccountRepository();
    }

    @Bean
    LedgerRepository ledgerRepository() {
        return new InMemoryLedgerRepository();
    }

    @Bean
    BatteryAssetRepository batteryAssetRepository() {
        return new InMemoryBatteryAssetRepository();
    }

    @Bean
    UsageEventRepository usageEventRepository() {
        return new InMemoryUsageEventRepository();
    }

    @Bean
    PerformEntitledSwap performEntitledSwap(
            EntitlementRepository entitlements,
            BatteryAssetRepository batteries,
            UsageEventRepository usages) {
        return new PerformEntitledSwap(entitlements, batteries, usages, Clock.systemUTC());
    }

    /** 正式种子：E-1 ACTIVE（U1）+ BAT-1 idle（形状对齐 PerformEntitledSwapTest；用户对齐 FormalLive U1）。 */
    @Bean
    ApplicationRunner seedEntitledSwap(
            EntitlementRepository entitlements, BatteryAssetRepository batteries) {
        return args -> {
            Instant now = Instant.now();
            entitlements.save(
                    Entitlement.rehydrate(
                            "E-1",
                            "O-1",
                            "U1",
                            "P-1",
                            now.minusSeconds(3600),
                            now.plusSeconds(86_400),
                            EntitlementStatus.ACTIVE));
            batteries.save(BatteryAsset.createIdle("BAT-1", "ORG-1", "vendor", "model"));
        };
    }
}
