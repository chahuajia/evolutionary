package com.evolutionary.credit.application;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerInvariant;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.domain.BillingStatement;
import com.evolutionary.credit.domain.CreditErrorCode;
import com.evolutionary.credit.domain.CreditLedgerDebt;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditProfile;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 账单全额还款（AC-51）。
 *
 * <p>阶段 6 不支持部分还款。
 */
public final class RepayBillingStatement {

    private final BillingStatementRepository statements;
    private final CreditLedgerDebtRepository debts;
    private final CreditProfileRepository profiles;
    private final AccountRepository accounts;
    private final LedgerRepository ledger;
    private final EntitlementRepository entitlements;
    private final Clock clock;

    public RepayBillingStatement(
            BillingStatementRepository statements,
            CreditLedgerDebtRepository debts,
            CreditProfileRepository profiles,
            AccountRepository accounts,
            LedgerRepository ledger,
            EntitlementRepository entitlements,
            Clock clock) {
        this.statements = Objects.requireNonNull(statements, "statements");
        this.debts = Objects.requireNonNull(debts, "debts");
        this.profiles = Objects.requireNonNull(profiles, "profiles");
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.ledger = Objects.requireNonNull(ledger, "ledger");
        this.entitlements = Objects.requireNonNull(entitlements, "entitlements");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public CreditOutcome<BillingStatement> execute(
            String userId, String statementId, Money amount) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(statementId, "statementId");
        Objects.requireNonNull(amount, "amount");

        BillingStatement statement = statements.get(statementId);
        if (!statement.userId().equals(userId)) {
            return CreditOutcome.err(CreditErrorCode.STATEMENT_NOT_DUE, "账单不属于该用户");
        }
        if (statement.status() != BillingStatement.Status.DUE
                && statement.status() != BillingStatement.Status.OVERDUE) {
            return CreditOutcome.err(CreditErrorCode.STATEMENT_NOT_DUE, "账单不可还款");
        }
        if (amount.cents() < statement.totalDue().cents()) {
            return CreditOutcome.err(
                    CreditErrorCode.PARTIAL_REPAY_NOT_ALLOWED, "阶段6不支持部分还款");
        }
        if (amount.cents() > statement.totalDue().cents()) {
            return CreditOutcome.err(
                    CreditErrorCode.PARTIAL_REPAY_NOT_ALLOWED, "还款金额须等于应还");
        }

        Account userBalance =
                accounts.findUserBalance(userId, statement.totalDue().currency());
        if (!userBalance.canCoverCents(amount.cents())) {
            return CreditOutcome.err(CreditErrorCode.CREDIT_NOT_AVAILABLE, "余额不足还款");
        }
        Account clearing =
                accounts.findOrgSettlement("CREDIT-CLEARING", amount.currency());

        var now = clock.instant();
        Account debited = userBalance.debit(amount.cents());
        Account credited = clearing.credit(amount.cents());
        ledger.append(
                LedgerEntry.creditStatementRepayment(
                        newId("led"),
                        userBalance.id(),
                        clearing.id(),
                        amount,
                        statement.id(),
                        now));
        accounts.save(debited);
        accounts.save(credited);
        LedgerInvariant.assertBalanced(ledger.findAll());

        List<CreditLedgerDebt> billed = debts.findByBilledStatementId(statementId);
        for (CreditLedgerDebt debt : billed) {
            debts.save(debt.markPaid(now));
        }

        CreditProfile profile = profiles.get(userId).repay(statement.totalDue());
        profiles.save(profile);

        for (Entitlement e :
                entitlements.findByUserIdAndStatus(userId, Entitlement.Status.FROZEN)) {
            entitlements.save(e.unfreeze());
        }

        BillingStatement paid = statement.markPaid(now);
        statements.save(paid);
        return CreditOutcome.ok(paid);
    }

    private static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
