package com.evolutionary.commerce.interfaces;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.BatteryAssetRepository;
import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.application.PerformEntitledSwap;
import com.evolutionary.commerce.application.ProductRepository;
import com.evolutionary.commerce.application.UsageEventRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.BatteryAsset;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.EntitlementStatus;
import com.evolutionary.commerce.domain.MeteringMode;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.commerce.domain.ProductStatus;
import com.evolutionary.commerce.infrastructure.InMemoryAccountRepository;
import com.evolutionary.commerce.infrastructure.InMemoryBatteryAssetRepository;
import com.evolutionary.commerce.infrastructure.InMemoryEntitlementRepository;
import com.evolutionary.commerce.infrastructure.InMemoryLedgerRepository;
import com.evolutionary.commerce.infrastructure.InMemoryProductRepository;
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
    ProductRepository productRepository() {
        return new InMemoryProductRepository();
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
            UsageEventRepository usages,
            ProductRepository products,
            AccountRepository accounts,
            LedgerRepository ledger) {
        return new PerformEntitledSwap(
                entitlements, batteries, usages, products, accounts, ledger, Clock.systemUTC());
    }

    /**
     * 正式种子：E-1 ACTIVE（非计量）+ BAT-1；计量独立面 P-M1 / E-M1 / BAT-M1 + ORG-1 SETTLEMENT。
     *
     * <p>用户余额 ACC-U1-BAL 由 CreditConfig 种子；计量结算走 ORG-1。
     */
    @Bean
    ApplicationRunner seedEntitledSwap(
            EntitlementRepository entitlements,
            BatteryAssetRepository batteries,
            ProductRepository products,
            AccountRepository accounts) {
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

            InMemoryProductRepository productStore = (InMemoryProductRepository) products;
            productStore.save(
                    Product.createMetered(
                            "P-M1", "ORG-1", "计量按电量", Money.cny(50), ProductStatus.PUBLISHED));
            entitlements.save(
                    Entitlement.rehydrate(
                            "E-M1",
                            "O-M1",
                            "U1",
                            "P-M1",
                            now.minusSeconds(3600),
                            null,
                            EntitlementStatus.ACTIVE,
                            null,
                            MeteringMode.PAY_AS_YOU_GO));
            batteries.save(BatteryAsset.createIdle("BAT-M1", "ORG-1", "vendor", "model"));
            accounts.save(
                    Account.open(
                            "ACC-ORG1-SETTLE",
                            AccountOwnerType.ORG,
                            "ORG-1",
                            AccountType.SETTLEMENT,
                            Currency.CNY,
                            0));
        };
    }
}
