package com.evolutionary.credit.infrastructure;

import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.CreditPolicyRepository;
import com.evolutionary.credit.domain.CreditPolicy;
import com.evolutionary.credit.domain.ScoreTier;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 信用政策 JPA 适配。 */
@Component
public final class JpaCreditPolicyRepository implements CreditPolicyRepository {

    private final CreditPolicyJpaRepository jpa;

    public JpaCreditPolicyRepository(CreditPolicyJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(CreditPolicy policy) {
        Currency currency = policy.limitFor(ScoreTier.A).currency();
        jpa.save(
                new CreditPolicyJpaEntity(
                        policy.id(),
                        policy.version(),
                        policy.limitFor(ScoreTier.A).cents(),
                        policy.limitFor(ScoreTier.B).cents(),
                        policy.limitFor(ScoreTier.C).cents(),
                        currency.name(),
                        policy.effectiveFrom()));
    }

    @Override
    public Optional<CreditPolicy> findByVersion(int version) {
        return jpa.findByVersion(version).map(JpaCreditPolicyRepository::toDomain);
    }

    private static CreditPolicy toDomain(CreditPolicyJpaEntity row) {
        Currency currency = Currency.valueOf(row.getCurrency());
        Map<ScoreTier, Money> limits = new EnumMap<>(ScoreTier.class);
        limits.put(ScoreTier.A, new Money(row.getLimitACents(), currency));
        limits.put(ScoreTier.B, new Money(row.getLimitBCents(), currency));
        limits.put(ScoreTier.C, new Money(row.getLimitCCents(), currency));
        return CreditPolicy.of(row.getId(), row.getVersion(), limits, row.getEffectiveFrom());
    }
}
