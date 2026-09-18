package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.application.ReferralBindingRepository;
import com.evolutionary.settlement.domain.ReferralBinding;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 推广绑定 JPA 适配。 */
@Component
public final class JpaReferralBindingRepository implements ReferralBindingRepository {

    private final ReferralBindingJpaRepository jpa;

    public JpaReferralBindingRepository(ReferralBindingJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(ReferralBinding binding) {
        jpa.save(
                new ReferralBindingJpaEntity(
                        binding.userId(),
                        binding.promoterOrgId(),
                        binding.boundAt(),
                        binding.expiresAt(),
                        binding.status()));
    }

    @Override
    public Optional<ReferralBinding> findActiveByUserId(String userId) {
        return jpa.findById(userId)
                .filter(row -> row.getStatus() == ReferralBinding.Status.ACTIVE)
                .map(JpaReferralBindingRepository::toDomain);
    }

    private static ReferralBinding toDomain(ReferralBindingJpaEntity row) {
        return ReferralBinding.rehydrate(
                row.getUserId(),
                row.getPromoterOrgId(),
                row.getBoundAt(),
                row.getExpiresAt(),
                row.getStatus());
    }
}
