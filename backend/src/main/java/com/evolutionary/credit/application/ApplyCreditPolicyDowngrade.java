package com.evolutionary.credit.application;

import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditPolicy;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.operator.application.AuditLogRepository;
import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * 将新政策限额应用到用户档案（AC-54）；不清零 usedCredit。
 *
 * <p>成功路径同事务记 {@link AuditAction#CREDIT_POLICY_DOWNGRADE}（切片32a）。
 */
public final class ApplyCreditPolicyDowngrade {

    private static final String SYSTEM_ACTOR = "system";
    private static final String SYSTEM_ORG = "PLATFORM";

    private final CreditPolicyRepository policies;
    private final CreditProfileRepository profiles;
    private final AuditLogRepository auditLogs;
    private final Clock clock;

    public ApplyCreditPolicyDowngrade(
            CreditPolicyRepository policies,
            CreditProfileRepository profiles,
            AuditLogRepository auditLogs,
            Clock clock) {
        this.policies = Objects.requireNonNull(policies, "policies");
        this.profiles = Objects.requireNonNull(profiles, "profiles");
        this.auditLogs = Objects.requireNonNull(auditLogs, "auditLogs");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public CreditOutcome<CreditProfile> execute(String userId, int policyVersion) {
        Objects.requireNonNull(userId, "userId");
        CreditPolicy policy = policies.getByVersion(policyVersion);
        CreditProfile profile = profiles.get(userId);
        CreditProfile updated =
                profile.withLimit(policy.limitFor(profile.scoreTier()), policy.version());
        profiles.save(updated);

        auditLogs.append(
                AuditLog.of(
                        UUID.randomUUID().toString(),
                        SYSTEM_ACTOR,
                        SYSTEM_ORG,
                        AuditAction.CREDIT_POLICY_DOWNGRADE,
                        "CreditProfile",
                        userId,
                        clock.instant()));

        return CreditOutcome.ok(updated);
    }
}
