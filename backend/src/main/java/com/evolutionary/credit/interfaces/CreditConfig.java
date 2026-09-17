package com.evolutionary.credit.interfaces;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.BillingStatementRepository;
import com.evolutionary.credit.application.CreditLedgerDebtRepository;
import com.evolutionary.credit.application.CreditProfileRepository;
import com.evolutionary.credit.application.MarkCreditOverdue;
import com.evolutionary.credit.application.RepayBillingStatement;
import com.evolutionary.credit.domain.BillingStatement;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.credit.domain.ScoreTier;
import com.evolutionary.credit.infrastructure.InMemoryBillingStatementRepository;
import com.evolutionary.credit.infrastructure.InMemoryCreditLedgerDebtRepository;
import com.evolutionary.credit.infrastructure.InMemoryCreditProfileRepository;
import java.time.Clock;
import java.time.Instant;
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
    BillingStatementRepository billingStatementRepository() {
        return new InMemoryBillingStatementRepository();
    }

    @Bean
    CreditLedgerDebtRepository creditLedgerDebtRepository() {
        return new InMemoryCreditLedgerDebtRepository();
    }

    /**
     * 逾期冻权益；注入与 {@code CommerceConfig} 同一 {@link EntitlementRepository} bean。
     */
    @Bean
    MarkCreditOverdue markCreditOverdue(
            BillingStatementRepository statements,
            CreditProfileRepository profiles,
            EntitlementRepository entitlements) {
        return new MarkCreditOverdue(statements, profiles, entitlements, Clock.systemUTC());
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

    /**
     * 正式本地种子：与 FE RSC /credit 对齐。
     *
     * <p>U1 limit=10000 / used=3000（分）；余额 5000 + CREDIT-CLEARING；STMT-2026-02 DUE 3000。
     */
    @Bean
    ApplicationRunner seedCredit(
            CreditProfileRepository profiles,
            BillingStatementRepository statements,
            AccountRepository accounts) {
        return args -> {
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
        };
    }
}
