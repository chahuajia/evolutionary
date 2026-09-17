package com.evolutionary.credit.interfaces;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.BillingStatementRepository;
import com.evolutionary.credit.application.CreditProfileRepository;
import com.evolutionary.credit.domain.BillingStatement;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.credit.domain.ScoreTier;
import com.evolutionary.credit.infrastructure.InMemoryBillingStatementRepository;
import com.evolutionary.credit.infrastructure.InMemoryCreditProfileRepository;
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

    /** 与 FE 原 fixture 对齐：U1 limit 100 / used 30（分）。 */
    @Bean
    ApplicationRunner seedCredit(
            CreditProfileRepository profiles, BillingStatementRepository statements) {
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
        };
    }
}
