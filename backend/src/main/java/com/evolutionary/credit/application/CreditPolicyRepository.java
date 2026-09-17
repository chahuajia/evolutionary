package com.evolutionary.credit.application;

import com.evolutionary.credit.domain.CreditPolicy;
import java.util.Optional;

public interface CreditPolicyRepository {
    void save(CreditPolicy policy);

    Optional<CreditPolicy> findByVersion(int version);

    default CreditPolicy getByVersion(int version) {
        return findByVersion(version)
                .orElseThrow(() -> new IllegalArgumentException("未知政策版本: " + version));
    }
}
