package com.evolutionary.credit.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerRefType;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.domain.BillingStatement;
import com.evolutionary.credit.domain.CreditLedgerDebt;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.credit.domain.ScoreTier;
import com.evolutionary.operator.infrastructure.InMemoryAuditLogRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AC-50 / AC-51。 */
class BillingAndRepayTest {

    private static final Instant T0 = Instant.parse("2026-02-01T00:00:00Z");
    private static final Instant JAN_START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant JAN_END = Instant.parse("2026-01-31T23:59:59Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);

    private InMemoryDebts debts;
    private InMemoryStatements statements;
    private InMemoryProfiles profiles;
    private InMemoryAccounts accounts;
    private InMemoryLedger ledger;
    private RunMonthlyBilling billing;
    private RepayBillingStatement repay;

    @BeforeEach
    void setUp() {
        debts = new InMemoryDebts();
        statements = new InMemoryStatements();
        profiles = new InMemoryProfiles();
        accounts = new InMemoryAccounts();
        ledger = new InMemoryLedger();
        billing =
                new RunMonthlyBilling(
                        debts, statements, new InMemoryAuditLogRepository(), CLOCK);
        repay =
                new RepayBillingStatement(
                        statements,
                        debts,
                        profiles,
                        accounts,
                        ledger,
                        new NoopEntitlements(),
                        CLOCK);

        CreditProfile base = CreditProfile.open("U1", Money.cny(10_000), ScoreTier.A, 1);
        CreditOutcome<CreditProfile> charged = base.charge(Money.cny(3_000));
        profiles.save(((CreditOutcome.Ok<CreditProfile>) charged).value());
        debts.save(
                CreditLedgerDebt.open(
                        "DEBT-1",
                        "U1",
                        "ORD-1",
                        Money.cny(3_000),
                        JAN_START.plusSeconds(86_400)));
        accounts.put(
                Account.open(
                        "ACC-U",
                        AccountOwnerType.USER,
                        "U1",
                        AccountType.BALANCE,
                        Currency.CNY,
                        5_000));
        accounts.put(
                Account.open(
                        "ACC-CLR",
                        AccountOwnerType.ORG,
                        "CREDIT-CLEARING",
                        AccountType.SETTLEMENT,
                        Currency.CNY,
                        0));
    }

    @Test
    @DisplayName("AC-50：OPEN Debt 出账 → Statement DUE，Debt BILLED")
    void monthlyBilling() {
        CreditOutcome<BillingStatement> outcome =
                billing.execute("U1", JAN_START, JAN_END);
        assertInstanceOf(CreditOutcome.Ok.class, outcome);
        BillingStatement stmt = ((CreditOutcome.Ok<BillingStatement>) outcome).value();

        assertEquals(BillingStatement.Status.DUE, stmt.status());
        assertEquals(3_000, stmt.totalDue().cents());
        assertEquals(CreditLedgerDebt.Status.BILLED, debts.get("DEBT-1").status());
        assertEquals(stmt.id(), debts.get("DEBT-1").billedStatementId());
    }

    @Test
    @DisplayName("AC-51：全额还款 → Statement PAID、Debt PAID、usedCredit=0、status good")
    void fullRepay() {
        BillingStatement due =
                ((CreditOutcome.Ok<BillingStatement>)
                                billing.execute("U1", JAN_START, JAN_END))
                        .value();

        CreditOutcome<BillingStatement> outcome =
                repay.execute("U1", due.id(), Money.cny(3_000));
        assertInstanceOf(CreditOutcome.Ok.class, outcome);
        BillingStatement paid = ((CreditOutcome.Ok<BillingStatement>) outcome).value();

        assertEquals(BillingStatement.Status.PAID, paid.status());
        assertEquals(CreditLedgerDebt.Status.PAID, debts.get("DEBT-1").status());
        assertEquals(0, profiles.get("U1").usedCredit().cents());
        assertEquals(CreditProfile.Status.GOOD, profiles.get("U1").status());
        assertEquals(2_000, accounts.get("ACC-U").balanceCents());
        assertEquals(
                1,
                ledger.findAll().stream()
                        .filter(e -> e.refType() == LedgerRefType.CREDIT_STATEMENT_REPAYMENT)
                        .count());
    }

    private static final class NoopEntitlements implements EntitlementRepository {
        @Override
        public void save(Entitlement entitlement) {}

        @Override
        public Entitlement get(String entitlementId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Entitlement> findByOrderId(String orderId) {
            return Optional.empty();
        }

        @Override
        public List<Entitlement> findActiveByUser(String userId) {
            return List.of();
        }
    }

    private static final class InMemoryDebts implements CreditLedgerDebtRepository {
        private final Map<String, CreditLedgerDebt> byId = new HashMap<>();

        @Override
        public void save(CreditLedgerDebt debt) {
            byId.put(debt.id(), debt);
        }

        @Override
        public Optional<CreditLedgerDebt> findById(String id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<CreditLedgerDebt> findByUserIdAndStatus(String userId, CreditLedgerDebt.Status status) {
            return byId.values().stream()
                    .filter(d -> d.userId().equals(userId) && d.status() == status)
                    .toList();
        }

        @Override
        public List<CreditLedgerDebt> findByStatus(CreditLedgerDebt.Status status) {
            return byId.values().stream().filter(d -> d.status() == status).toList();
        }

        @Override
        public List<CreditLedgerDebt> findByBilledStatementId(String statementId) {
            return byId.values().stream()
                    .filter(d -> statementId.equals(d.billedStatementId()))
                    .toList();
        }

        @Override
        public List<CreditLedgerDebt> findByOrderId(String orderId) {
            return byId.values().stream().filter(d -> d.orderId().equals(orderId)).toList();
        }
    }

    private static final class InMemoryStatements implements BillingStatementRepository {
        private final Map<String, BillingStatement> byId = new HashMap<>();

        @Override
        public void save(BillingStatement statement) {
            byId.put(statement.id(), statement);
        }

        @Override
        public Optional<BillingStatement> findById(String id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<BillingStatement> findByUserId(String userId) {
            return byId.values().stream().filter(s -> s.userId().equals(userId)).toList();
        }
    }

    private static final class InMemoryProfiles implements CreditProfileRepository {
        private final Map<String, CreditProfile> byUser = new HashMap<>();

        @Override
        public Optional<CreditProfile> findByUserId(String userId) {
            return Optional.ofNullable(byUser.get(userId));
        }

        @Override
        public void save(CreditProfile profile) {
            byUser.put(profile.userId(), profile);
        }
    }

    private static final class InMemoryAccounts implements AccountRepository {
        private final Map<String, Account> byId = new HashMap<>();

        void put(Account a) {
            byId.put(a.id(), a);
        }

        @Override
        public Account get(String accountId) {
            return byId.get(accountId);
        }

        @Override
        public void save(Account account) {
            byId.put(account.id(), account);
        }

        @Override
        public Account findUserBalance(String userId, Currency currency) {
            return byId.values().stream()
                    .filter(a -> a.ownerType() == AccountOwnerType.USER)
                    .filter(a -> a.ownerId().equals(userId))
                    .filter(a -> a.type() == AccountType.BALANCE)
                    .filter(a -> a.currency() == currency)
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public Account findUserPoints(String userId, Currency currency) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Account findOrgSettlement(String orgId, Currency currency) {
            return byId.values().stream()
                    .filter(a -> a.ownerType() == AccountOwnerType.ORG)
                    .filter(a -> a.ownerId().equals(orgId))
                    .filter(a -> a.type() == AccountType.SETTLEMENT)
                    .filter(a -> a.currency() == currency)
                    .findFirst()
                    .orElseThrow();
        }
    }

    private static final class InMemoryLedger implements LedgerRepository {
        private final List<LedgerEntry> entries = new ArrayList<>();

        @Override
        public void append(LedgerEntry entry) {
            entries.add(entry);
        }

        @Override
        public List<LedgerEntry> findAll() {
            return List.copyOf(entries);
        }

        @Override
        public List<LedgerEntry> findByOrderId(String orderId) {
            return entries.stream().filter(e -> e.refId().equals(orderId)).toList();
        }
    }
}
