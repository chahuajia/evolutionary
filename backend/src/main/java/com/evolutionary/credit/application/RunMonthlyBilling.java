package com.evolutionary.credit.application;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.domain.BillingStatement;
import com.evolutionary.credit.domain.CreditErrorCode;
import com.evolutionary.credit.domain.CreditLedgerDebt;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.operator.application.AuditLogRepository;
import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 月度出账：OPEN Debt → Statement DUE + Debt BILLED（AC-50）。
 * 成功记 {@link AuditAction#CREDIT_MONTHLY_BILLING}（切片33b）。
 */
public final class RunMonthlyBilling {

    private static final String SYSTEM_ACTOR = "system";
    private static final String PLATFORM_ORG = "PLATFORM";

    private final CreditLedgerDebtRepository debts;
    private final BillingStatementRepository statements;
    private final AuditLogRepository auditLogs;
    private final Clock clock;

    public RunMonthlyBilling(
            CreditLedgerDebtRepository debts,
            BillingStatementRepository statements,
            AuditLogRepository auditLogs,
            Clock clock) {
        this.debts = Objects.requireNonNull(debts, "debts");
        this.statements = Objects.requireNonNull(statements, "statements");
        this.auditLogs = Objects.requireNonNull(auditLogs, "auditLogs");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @param periodStart 账期起
     * @param periodEnd 账期止（通常为上月末）
     */
    public CreditOutcome<BillingStatement> execute(
            String userId, Instant periodStart, Instant periodEnd) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(periodStart, "periodStart");
        Objects.requireNonNull(periodEnd, "periodEnd");

        List<CreditLedgerDebt> open =
                debts.findByUserIdAndStatus(userId, CreditLedgerDebt.Status.OPEN);
        if (open.isEmpty()) {
            return CreditOutcome.err(CreditErrorCode.CREDIT_NOT_AVAILABLE, "无待出账负债");
        }

        long total = open.stream().mapToLong(d -> d.amount().cents()).sum();
        Instant now = clock.instant();
        String statementId = "stmt-" + UUID.randomUUID();
        BillingStatement statement =
                BillingStatement.issueDue(
                        statementId,
                        userId,
                        periodStart,
                        periodEnd,
                        Money.cny(total),
                        now);

        for (CreditLedgerDebt debt : open) {
            debts.save(debt.markBilled(statementId));
        }
        statements.save(statement);

        auditLogs.append(
                AuditLog.of(
                        UUID.randomUUID().toString(),
                        SYSTEM_ACTOR,
                        PLATFORM_ORG,
                        AuditAction.CREDIT_MONTHLY_BILLING,
                        "BillingStatement",
                        statementId,
                        now));

        return CreditOutcome.ok(statement);
    }
}
