package com.evolutionary.credit.application;

import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditPolicy;
import com.evolutionary.credit.domain.CreditProfile;
import java.util.Objects;

/** 将新政策限额应用到用户档案（AC-54）；不清零 usedCredit。 */
public final class ApplyCreditPolicyDowngrade {

    private final CreditPolicyRepository policies;
    private final CreditProfileRepository profiles;

    public ApplyCreditPolicyDowngrade(
            CreditPolicyRepository policies, CreditProfileRepository profiles) {
        this.policies = Objects.requireNonNull(policies, "policies");
        this.profiles = Objects.requireNonNull(profiles, "profiles");
    }

    public CreditOutcome<CreditProfile> execute(String userId, int policyVersion) {
        Objects.requireNonNull(userId, "userId");
        CreditPolicy policy = policies.getByVersion(policyVersion);
        CreditProfile profile = profiles.get(userId);
        CreditProfile updated =
                profile.withLimit(policy.limitFor(profile.scoreTier()), policy.version());
        profiles.save(updated);
        return CreditOutcome.ok(updated);
    }
}
