package com.evolutionary.credit.application;

import com.evolutionary.credit.domain.CreditProfile;
import java.util.Optional;

public interface CreditProfileRepository {
    Optional<CreditProfile> findByUserId(String userId);

    void save(CreditProfile profile);

    default CreditProfile get(String userId) {
        return findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("未知信用档案: " + userId));
    }
}
