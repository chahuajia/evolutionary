package com.evolutionary.credit.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.BatteryAssetRepository;
import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.application.PerformEntitledSwap;
import com.evolutionary.commerce.application.ProductRepository;
import com.evolutionary.commerce.application.UsageEventRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.BatteryAsset;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.commerce.domain.UsageEvent;
import com.evolutionary.credit.domain.BillingStatement;
import com.evolutionary.credit.domain.CreditErrorCode;
import com.evolutionary.credit.domain.CreditLedgerDebt;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditPolicy;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.credit.domain.ScoreTier;
import com.evolutionary.operator.application.AuditLogRepository;
import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
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

/** AC-52 / AC-53 / AC-54。 */
class CreditOverdueAndPolicyTest {

    private static final Instant BILL_AT = Instant.parse("2026-02-01T00:00:00Z");
    private static final Instant PAST_DUE = Instant.parse("2026-02-10T00:00:00Z");
    private static final Instant JAN_START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant JAN_END = Instant.parse("2026-01-31T23:59:59Z");

    private InMemoryDebts debts;
    private InMemoryStatements statements;
    private InMemoryProfiles profiles;
    private InMemoryEntitlements entitlements;
    private InMemoryAccounts accounts;
    private InMemoryLedger ledger;
    private InMemoryPolicies policies;
    private InMemoryProducts products;
    private InMemoryBatteries batteries;
    private InMemoryUsages usages;
    private InMemoryAuditLogs auditLogs;
    private RunMonthlyBilling billing;
    private MarkCreditOverdue markOverdue;
    private RepayBillingStatement repay;
    private ApplyCreditPolicyDowngrade applyPolicy;
    private PurchaseWithCredit purchase;
    private PerformEntitledSwap swap;

    @BeforeEach
    void setUp() {
        debts = new InMemoryDebts();
        statements = new InMemoryStatements();
        profiles = new InMemoryProfiles();
        entitlements = new InMemoryEntitlements();
        accounts = new InMemoryAccounts();
        ledger = new InMemoryLedger();
        policies = new InMemoryPolicies();
        products = new InMemoryProducts();
        batteries = new InMemoryBatteries();
        usages = new InMemoryUsages();
        auditLogs = new InMemoryAuditLogs();

        Clock billClock = Clock.fixed(BILL_AT, ZoneOffset.UTC);
        billing = new RunMonthlyBilling(debts, statements, auditLogs, billClock);
        markOverdue =
                new MarkCreditOverdue(
                        statements,
                        profiles,
                        entitlements,
                        auditLogs,
                        Clock.fixed(PAST_DUE, ZoneOffset.UTC));
        repay =
                new RepayBillingStatement(
                        statements,
                        debts,
                        profiles,
                        accounts,
                        ledger,
                        entitlements,
                        Clock.fixed(PAST_DUE, ZoneOffset.UTC));
        applyPolicy =
                new ApplyCreditPolicyDowngrade(
                        policies, profiles, auditLogs, Clock.fixed(PAST_DUE, ZoneOffset.UTC));
        purchase =
                new PurchaseWithCredit(
                        products,
                        new InMemoryOrders(),
                        entitlements,
                        ledger,
                        profiles,
                        debts,
                        billClock);
        swap =
                new PerformEntitledSwap(
                        entitlements,
                        batteries,
                        usages,
                        Clock.fixed(PAST_DUE, ZoneOffset.UTC));

        products.put(
                Product.create(
                        "P1", "ORG-1", "月卡", Money.cny(3_000), 90, Product.Status.PUBLISHED));
        profiles.save(CreditProfile.open("U1", Money.cny(10_000), ScoreTier.A, 1));
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
        accounts.put(
                Account.open(
                        "ACC-O",
                        AccountOwnerType.ORG,
                        "ORG-1",
                        AccountType.SETTLEMENT,
                        Currency.CNY,
                        0));
        batteries.put(BatteryAsset.createIdle("BAT-1", "ORG-1", "v", "m"));
    }

    @Test
    @DisplayName("AC-52：逾期 → profile overdue、权益 FROZEN、swap CREDIT_OVERDUE_BLOCKED")
    void overdueFreezesAndBlocksSwap() {
        seedOpenDebtAndActiveEntitlement();
        BillingStatement due =
                ((CreditOutcome.Ok<BillingStatement>)
                                billing.execute("U1", JAN_START, JAN_END))
                        .value();

        CreditOutcome<CreditProfile> marked = markOverdue.execute("U1", due.id());
        assertInstanceOf(CreditOutcome.Ok.class, marked);
        assertEquals(CreditProfile.Status.OVERDUE, profiles.get("U1").status());
        assertEquals(BillingStatement.Status.OVERDUE, statements.get(due.id()).status());
        assertEquals(Entitlement.Status.FROZEN, entitlements.get("ENT-1").status());
        List<AuditLog> overdueAudit =
                auditLogs.findByResourceId(due.id()).stream()
                        .filter(l -> l.action() == AuditAction.CREDIT_MARK_OVERDUE)
                        .toList();
        assertEquals(1, overdueAudit.size());
        assertEquals(AuditAction.CREDIT_MARK_OVERDUE, overdueAudit.get(0).action());
        assertEquals("BillingStatement", overdueAudit.get(0).resourceType());
        assertEquals("system", overdueAudit.get(0).actorUserId());
        assertEquals("PLATFORM", overdueAudit.get(0).orgId());

        DomainOutcome<?> swapOut = swap.execute("U1", "ENT-1", "CAB-1");
        assertInstanceOf(DomainOutcome.Err.class, swapOut);
        assertEquals(
                DomainErrorCode.CREDIT_OVERDUE_BLOCKED,
                ((DomainOutcome.Err<?>) swapOut).code());
    }

    @Test
    @DisplayName("AC-53：还款后 good + 权益 ACTIVE，可再 swap")
    void repayUnfreezes() {
        seedOpenDebtAndActiveEntitlement();
        BillingStatement due =
                ((CreditOutcome.Ok<BillingStatement>)
                                billing.execute("U1", JAN_START, JAN_END))
                        .value();
        markOverdue.execute("U1", due.id());

        CreditOutcome<BillingStatement> repaid =
                repay.execute("U1", due.id(), Money.cny(3_000));
        assertInstanceOf(CreditOutcome.Ok.class, repaid);
        assertEquals(CreditProfile.Status.GOOD, profiles.get("U1").status());
        assertEquals(Entitlement.Status.ACTIVE, entitlements.get("ENT-1").status());

        DomainOutcome<?> swapOut = swap.execute("U1", "ENT-1", "CAB-1");
        assertInstanceOf(DomainOutcome.Ok.class, swapOut);
    }

    @Test
    @DisplayName("AC-54：政策降额不清零 usedCredit；新购拒绝")
    void policyDowngradeBlocksNewPurchase() {
        CreditProfile charged =
                ((CreditOutcome.Ok<CreditProfile>)
                                CreditProfile.open("U1", Money.cny(10_000), ScoreTier.A, 1)
                                        .charge(Money.cny(3_000)))
                        .value();
        profiles.save(charged);
        debts.save(
                CreditLedgerDebt.open(
                        "DEBT-1", "U1", "ORD-1", Money.cny(3_000), JAN_START));

        policies.save(
                CreditPolicy.of(
                        "POL-2",
                        2,
                        Map.of(
                                ScoreTier.A, Money.cny(2_000),
                                ScoreTier.B, Money.cny(1_000),
                                ScoreTier.C, Money.cny(500)),
                        BILL_AT));

        CreditOutcome<CreditProfile> applied = applyPolicy.execute("U1", 2);
        assertInstanceOf(CreditOutcome.Ok.class, applied);
        CreditProfile after = ((CreditOutcome.Ok<CreditProfile>) applied).value();
        assertEquals(2_000, after.creditLimit().cents());
        assertEquals(3_000, after.usedCredit().cents());
        assertEquals(2, after.policyVersion());
        List<AuditLog> downgradeAudit = auditLogs.findByResourceId("U1");
        assertEquals(1, downgradeAudit.size());
        assertEquals(AuditAction.CREDIT_POLICY_DOWNGRADE, downgradeAudit.get(0).action());
        assertEquals("CreditProfile", downgradeAudit.get(0).resourceType());
        assertEquals("system", downgradeAudit.get(0).actorUserId());
        assertEquals("PLATFORM", downgradeAudit.get(0).orgId());

        CreditOutcome<CreditPurchaseResult> buy = purchase.execute("U1", "P1");
        assertInstanceOf(CreditOutcome.Err.class, buy);
        assertEquals(
                CreditErrorCode.CREDIT_LIMIT_EXCEEDED,
                ((CreditOutcome.Err<CreditPurchaseResult>) buy).code());
        assertEquals(CreditLedgerDebt.Status.OPEN, debts.get("DEBT-1").status());
    }

    private void seedOpenDebtAndActiveEntitlement() {
        CreditProfile charged =
                ((CreditOutcome.Ok<CreditProfile>)
                                profiles.get("U1").charge(Money.cny(3_000)))
                        .value();
        profiles.save(charged);
        debts.save(
                CreditLedgerDebt.open(
                        "DEBT-1", "U1", "ORD-1", Money.cny(3_000), JAN_START));
        Order paid =
                Order.create("ORD-1", "U1", "P1", "ORG-1", Money.cny(3_000), JAN_START)
                        .pay(JAN_START);
        Entitlement ent =
                Entitlement.createActive("ENT-1", paid, products.get("P1"), JAN_START);
        entitlements.save(ent);
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

    private static final class InMemoryEntitlements implements EntitlementRepository {
        private final Map<String, Entitlement> byId = new HashMap<>();

        @Override
        public void save(Entitlement entitlement) {
            byId.put(entitlement.id(), entitlement);
        }

        @Override
        public Entitlement get(String entitlementId) {
            return byId.get(entitlementId);
        }

        @Override
        public Optional<Entitlement> findByOrderId(String orderId) {
            return byId.values().stream().filter(e -> e.orderId().equals(orderId)).findFirst();
        }

        @Override
        public List<Entitlement> findActiveByUser(String userId) {
            return byId.values().stream()
                    .filter(e -> e.userId().equals(userId))
                    .filter(e -> e.status() == Entitlement.Status.ACTIVE)
                    .toList();
        }

        @Override
        public List<Entitlement> findByUserIdAndStatus(String userId, Entitlement.Status status) {
            return byId.values().stream()
                    .filter(e -> e.userId().equals(userId) && e.status() == status)
                    .toList();
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

    private static final class InMemoryPolicies implements CreditPolicyRepository {
        private final Map<Integer, CreditPolicy> byVersion = new HashMap<>();

        @Override
        public void save(CreditPolicy policy) {
            byVersion.put(policy.version(), policy);
        }

        @Override
        public Optional<CreditPolicy> findByVersion(int version) {
            return Optional.ofNullable(byVersion.get(version));
        }
    }

    private static final class InMemoryProducts implements ProductRepository {
        private final Map<String, Product> byId = new HashMap<>();

        void put(Product p) {
            byId.put(p.id(), p);
        }

        @Override
        public void save(Product product) {
            put(product);
        }

        @Override
        public Product get(String productId) {
            return byId.get(productId);
        }
    }

    private static final class InMemoryOrders
            implements com.evolutionary.commerce.application.OrderRepository {
        private final Map<String, Order> byId = new HashMap<>();

        @Override
        public void save(Order order) {
            byId.put(order.id(), order);
        }

        @Override
        public Order get(String orderId) {
            return byId.get(orderId);
        }
    }

    private static final class InMemoryBatteries implements BatteryAssetRepository {
        private final Map<String, BatteryAsset> byId = new HashMap<>();

        void put(BatteryAsset b) {
            byId.put(b.id(), b);
        }

        @Override
        public Optional<BatteryAsset> findAnyIdle() {
            return byId.values().stream().filter(BatteryAsset::isIdle).findFirst();
        }

        @Override
        public BatteryAsset get(String batteryId) {
            return byId.get(batteryId);
        }

        @Override
        public void save(BatteryAsset battery) {
            byId.put(battery.id(), battery);
        }
    }

    private static final class InMemoryUsages implements UsageEventRepository {
        private final List<UsageEvent> events = new ArrayList<>();

        @Override
        public void save(UsageEvent event) {
            events.add(event);
        }

        @Override
        public Optional<UsageEvent> findStartedByBattery(String batteryId) {
            return events.stream()
                    .filter(e -> e.batteryId().equals(batteryId) && e.isStarted())
                    .findFirst();
        }

        @Override
        public List<UsageEvent> findStartedByUser(String userId) {
            return events.stream()
                    .filter(e -> e.userId().equals(userId) && e.isStarted())
                    .toList();
        }

        @Override
        public Optional<UsageEvent> findStartedByEntitlement(String entitlementId) {
            return events.stream()
                    .filter(e -> e.entitlementId().equals(entitlementId) && e.isStarted())
                    .findFirst();
        }
    }

    private static final class InMemoryAuditLogs implements AuditLogRepository {
        private final List<AuditLog> logs = new ArrayList<>();

        @Override
        public void append(AuditLog log) {
            logs.add(log);
        }

        @Override
        public List<AuditLog> findByResourceId(String resourceId) {
            return logs.stream().filter(l -> l.resourceId().equals(resourceId)).toList();
        }
    }
}
