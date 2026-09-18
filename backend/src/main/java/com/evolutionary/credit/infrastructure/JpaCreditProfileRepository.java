package com.evolutionary.credit.infrastructure;

import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.CreditProfileRepository;
import com.evolutionary.credit.domain.CreditProfile;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 信用档案 JPA 适配。 */
@Component
public final class JpaCreditProfileRepository implements CreditProfileRepository {

    private final CreditProfileJpaRepository jpa;

    public JpaCreditProfileRepository(CreditProfileJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<CreditProfile> findByUserId(String userId) {
        return jpa.findById(userId).map(JpaCreditProfileRepository::toDomain);
    }

    @Override
    public void save(CreditProfile profile) {
        jpa.save(
                new CreditProfileJpaEntity(
                        profile.userId(),
                        profile.creditLimit().cents(),
                        profile.creditLimit().currency().name(),
                        profile.usedCredit().cents(),
                        profile.usedCredit().currency().name(),
                        profile.status(),
                        profile.scoreTier(),
                        profile.policyVersion()));
    }

    private static CreditProfile toDomain(CreditProfileJpaEntity row) {
        Money creditLimit =
                new Money(row.getCreditLimitCents(), Currency.valueOf(row.getCreditLimitCurrency()));
        Money usedCredit =
                new Money(row.getUsedCreditCents(), Currency.valueOf(row.getUsedCreditCurrency()));
        return CreditProfile.rehydrate(
                row.getUserId(),
                creditLimit,
                usedCredit,
                row.getStatus(),
                row.getScoreTier(),
                row.getPolicyVersion());
    }
}
