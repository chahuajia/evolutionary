package com.evolutionary.credit.interfaces;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.application.OrderRepository;
import com.evolutionary.commerce.application.ProductRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.commerce.domain.ProductStatus;
import com.evolutionary.commerce.infrastructure.InMemoryOrderRepository;
import com.evolutionary.credit.application.ApplyCreditPolicyDowngrade;
import com.evolutionary.credit.application.BillingStatementRepository;
import com.evolutionary.credit.application.CreditLedgerDebtRepository;
import com.evolutionary.credit.application.CreditPolicyRepository;
import com.evolutionary.credit.application.CreditProfileRepository;
import com.evolutionary.credit.application.MarkCreditOverdue;
import com.evolutionary.credit.application.PurchaseWithCredit;
import com.evolutionary.credit.application.RepayBillingStatement;
import com.evolutionary.credit.application.RunMonthlyBilling;
import com.evolutionary.credit.domain.BillingStatement;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditPolicy;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.credit.domain.ScoreTier;
import com.evolutionary.credit.infrastructure.InMemoryBillingStatementRepository;
import com.evolutionary.credit.infrastructure.InMemoryCreditLedgerDebtRepository;
import com.evolutionary.credit.infrastructure.InMemoryCreditPolicyRepository;
import com.evolutionary.credit.infrastructure.InMemoryCreditProfileRepository;
import com.evolutionary.operator.application.AuditLogRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreditConfig {

    @Bean
    CreditProfileRepository creditProfileRepository() {
        return new InMemoryCreditProfileRepository();
    }

    @Bean
    CreditPolicyRepository creditPolicyRepository() {
        return new InMemoryCreditPolicyRepository();
    }

    @Bean
    BillingStatementRepository billingStatementRepository() {
        return new InMemoryBillingStatementRepository();
    }

    @Bean
    CreditLedgerDebtRepository creditLedgerDebtRepository() {
        return new InMemoryCreditLedgerDebtRepository();
    }

    /** 与 Commerce 共用写单面；bean 放 Credit 以免改 CommerceConfig 种子。 */
    @Bean
    OrderRepository orderRepository() {
        return new InMemoryOrderRepository();
    }

    /**
     * 逾期冻权益；注入与 {@code CommerceConfig} 同一 {@link EntitlementRepository} bean。
     *
     * <p>审计写入与 Operator 共用 {@link AuditLogRepository}（切片32a）。
     */
    @Bean
    MarkCreditOverdue markCreditOverdue(
            BillingStatementRepository statements,
            CreditProfileRepository profiles,
            EntitlementRepository entitlements,
            AuditLogRepository auditLogs) {
        return new MarkCreditOverdue(
                statements, profiles, entitlements, auditLogs, Clock.systemUTC());
    }

    @Bean
    RepayBillingStatement repayBillingStatement(
            BillingStatementRepository statements,
            CreditLedgerDebtRepository debts,
            CreditProfileRepository profiles,
            AccountRepository accounts,
            LedgerRepository ledger,
            EntitlementRepository entitlements) {
        return new RepayBillingStatement(
                statements, debts, profiles, accounts, ledger, entitlements, Clock.systemUTC());
    }

    @Bean
    PurchaseWithCredit purchaseWithCredit(
            ProductRepository products,
            OrderRepository orders,
            EntitlementRepository entitlements,
            LedgerRepository ledger,
            CreditProfileRepository profiles,
            CreditLedgerDebtRepository debts) {
        return new PurchaseWithCredit(
                products, orders, entitlements, ledger, profiles, debts, Clock.systemUTC());
    }

    /** 月度出账：OPEN Debt → Statement DUE（AC-50）。 */
    @Bean
    RunMonthlyBilling runMonthlyBilling(
            CreditLedgerDebtRepository debts,
            BillingStatementRepository statements,
            AuditLogRepository auditLogs) {
        return new RunMonthlyBilling(debts, statements, auditLogs, Clock.systemUTC());
    }

    /** 政策降额应用到档案（AC-54）；不清零 usedCredit；成功记 CREDIT_POLICY_DOWNGRADE。 */
    @Bean
    ApplyCreditPolicyDowngrade applyCreditPolicyDowngrade(
            CreditPolicyRepository policies,
            CreditProfileRepository profiles,
            AuditLogRepository auditLogs) {
        return new ApplyCreditPolicyDowngrade(policies, profiles, auditLogs, Clock.systemUTC());
    }

    /**
     * 正式本地种子：与 FE RSC /credit 对齐。
     *
     * <p>U1 limit=10000 / used=3000（分）；余额 5000 + CREDIT-CLEARING；STMT-2026-02 DUE 3000。
     *
     * <p>P-CREDIT-1：FIXED 非计量 3000¢（可用额度 7000，可购一次）；写入同一 {@link ProductRepository}。
     *
     * <p>政策 v2：A 档限额 8000（低于 U1 当前 10000），便于 apply-policy IT。
     */
    @Bean
    ApplicationRunner seedCredit(
            CreditProfileRepository profiles,
            CreditPolicyRepository policies,
            BillingStatementRepository statements,
            AccountRepository accounts,
            ProductRepository products) {
        return args -> {
            policies.save(
                    CreditPolicy.of(
                            "POL-2",
                            2,
                            Map.of(
                                    ScoreTier.A, Money.cny(8_000),
                                    ScoreTier.B, Money.cny(5_000),
                                    ScoreTier.C, Money.cny(2_000)),
                            Instant.parse("2026-03-01T00:00:00Z")));

            CreditProfile profile =
                    CreditProfile.open("U1", Money.cny(10_000), ScoreTier.A, 1);
            CreditOutcome<CreditProfile> charged = profile.charge(Money.cny(3_000));
            if (charged instanceof CreditOutcome.Ok<CreditProfile> ok) {
                profiles.save(ok.value());
            } else {
                profiles.save(profile);
            }

            statements.save(
                    BillingStatement.issueDue(
                            "STMT-2026-02",
                            "U1",
                            Instant.parse("2026-01-01T00:00:00Z"),
                            Instant.parse("2026-01-31T00:00:00Z"),
                            Money.cny(3_000),
                            Instant.parse("2026-02-01T00:00:00Z")));

            statements.save(
                    BillingStatement.issueDue(
                                    "STMT-2025-12",
                                    "U1",
                                    Instant.parse("2025-11-01T00:00:00Z"),
                                    Instant.parse("2025-11-30T00:00:00Z"),
                                    Money.cny(2_500),
                                    Instant.parse("2025-12-01T00:00:00Z"))
                            .markPaid(Instant.parse("2025-12-10T08:30:00Z")));

            accounts.save(
                    Account.open(
                            "ACC-U1-BAL",
                            AccountOwnerType.USER,
                            "U1",
                            AccountType.BALANCE,
                            Currency.CNY,
                            5_000));
            accounts.save(
                    Account.open(
                            "ACC-CLR",
                            AccountOwnerType.ORG,
                            "CREDIT-CLEARING",
                            AccountType.SETTLEMENT,
                            Currency.CNY,
                            0));

            products.save(
                    Product.create(
                            "P-CREDIT-1",
                            "ORG-1",
                            "信用购月卡",
                            Money.cny(3_000),
                            30,
                            ProductStatus.PUBLISHED));
        };
    }
}
