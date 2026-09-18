package com.evolutionary.commerce.interfaces;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.BatteryAssetRepository;
import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.application.OrderRepository;
import com.evolutionary.commerce.application.PerformEntitledSwap;
import com.evolutionary.commerce.application.ProductRepository;
import com.evolutionary.commerce.application.RefundOrder;
import com.evolutionary.commerce.application.ResolveUserWallet;
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
import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommerceConfig {

    /** EntitlementRepository → {@code JpaEntitlementRepository}（表 entitlements）。 */

    /** AccountRepository → {@code JpaAccountRepository}（表 accounts）。 */

    /** LedgerRepository → {@code JpaLedgerRepository}（表 ledger_entries）。 */

    /** ProductRepository → {@code JpaProductRepository}（表 products）。 */

    /** BatteryAssetRepository → {@code JpaBatteryAssetRepository}（表 battery_assets）。 */

    /** OrderRepository → {@code JpaOrderRepository}（表 orders）。 */

    /** UsageEventRepository → {@code JpaUsageEventRepository}（表 usage_events）。 */

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

    /** 切片20a / AC-20：订单退款（余额/积分逆序；信用购无支付分录亦可）。 */
    @Bean
    RefundOrder refundOrder(
            OrderRepository orders,
            EntitlementRepository entitlements,
            UsageEventRepository usages,
            AccountRepository accounts,
            LedgerRepository ledger) {
        return new RefundOrder(
                orders, entitlements, usages, accounts, ledger, Clock.systemUTC());
    }

    /** 切片27b：消费者钱包读模型。 */
    @Bean
    ResolveUserWallet resolveUserWallet(AccountRepository accounts) {
        return new ResolveUserWallet(accounts);
    }

    /**
     * 正式种子：E-1 UNLIMITED + E-FINITE FINITE ACTIVE（U1）+ BAT-1；计量独立面 P-M1 / E-M1 /
     * BAT-M1 + ORG-1 SETTLEMENT。
     *
     * <p>用户余额 ACC-U1-BAL 由 CreditConfig 种子；ACC-U1-PTS 本种子（切片27b）；计量结算走 ORG-1。
     * E-FINITE 供 AC-14 默认选卡。
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
            // 切片12a / AC-14：FINITE 与 E-1 并存，默认选卡优先次卡
            entitlements.save(
                    Entitlement.rehydrate(
                            "E-FINITE",
                            "O-FINITE",
                            "U1",
                            "P-FINITE",
                            now.minusSeconds(3600),
                            now.plusSeconds(86_400),
                            EntitlementStatus.ACTIVE,
                            5));
            batteries.save(BatteryAsset.createIdle("BAT-1", "ORG-1", "vendor", "model"));

            products.save(
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
            // 切片27b：U1 积分账户（余额仍由 CreditConfig ACC-U1-BAL）
            accounts.save(
                    Account.open(
                            "ACC-U1-PTS",
                            AccountOwnerType.USER,
                            "U1",
                            AccountType.POINTS,
                            Currency.CNY,
                            0));
        };
    }
}
