package com.evolutionary.credit.application;

import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.credit.domain.BillingStatement;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.credit.domain.StatementStatus;
import com.evolutionary.operator.application.AuditLogRepository;
import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 逾期任务（AC-52）：DUE 账单过 dueDate → profile overdue + ACTIVE 权益 FROZEN。
 *
 * <p>成功路径同事务记 {@link AuditAction#CREDIT_MARK_OVERDUE}（切片32a）。
 */
public final class MarkCreditOverdue {

    private static final String SYSTEM_ACTOR = "system";
    private static final String SYSTEM_ORG = "PLATFORM";

    private final BillingStatementRepository statements;
    private final CreditProfileRepository profiles;
    private final EntitlementRepository entitlements;
    private final AuditLogRepository auditLogs;
    private final Clock clock;

    public MarkCreditOverdue(
            BillingStatementRepository statements,
            CreditProfileRepository profiles,
            EntitlementRepository entitlements,
            AuditLogRepository auditLogs,
            Clock clock) {
        this.statements = Objects.requireNonNull(statements, "statements");
        this.profiles = Objects.requireNonNull(profiles, "profiles");
        this.entitlements = Objects.requireNonNull(entitlements, "entitlements");
        this.auditLogs = Objects.requireNonNull(auditLogs, "auditLogs");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public CreditOutcome<CreditProfile> execute(String userId, String statementId) {
        Objects.requireNonNull(userId, "userId");
        BillingStatement statement = statements.get(statementId);
        if (!statement.userId().equals(userId)) {
            return CreditOutcome.err(
                    com.evolutionary.credit.domain.CreditErrorCode.STATEMENT_NOT_DUE,
                    "账单不属于该用户");
        }
        if (statement.status() != StatementStatus.DUE) {
            return CreditOutcome.err(
                    com.evolutionary.credit.domain.CreditErrorCode.STATEMENT_NOT_DUE,
                    "仅 DUE 账单可逾期");
        }
        if (!statement.isPastDue(clock.instant())) {
            return CreditOutcome.err(
                    com.evolutionary.credit.domain.CreditErrorCode.STATEMENT_NOT_DUE,
                    "尚未过 dueDate");
        }

        statements.save(statement.markOverdue());
        CreditProfile overdue = profiles.get(userId).markOverdue();
        profiles.save(overdue);

        List<Entitlement> frozen = new ArrayList<>();
        for (Entitlement e : entitlements.findActiveByUser(userId)) {
            Entitlement f = e.freeze();
            entitlements.save(f);
            frozen.add(f);
        }

        auditLogs.append(
                AuditLog.of(
                        UUID.randomUUID().toString(),
                        SYSTEM_ACTOR,
                        SYSTEM_ORG,
                        AuditAction.CREDIT_MARK_OVERDUE,
                        "BillingStatement",
                        statementId,
                        clock.instant()));

        return CreditOutcome.ok(overdue);
    }
}
