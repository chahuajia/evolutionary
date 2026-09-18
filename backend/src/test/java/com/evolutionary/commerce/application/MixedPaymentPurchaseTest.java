package com.evolutionary.commerce.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.EntitlementStatus;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerInvariant;
import com.evolutionary.commerce.domain.LedgerRefType;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.commerce.domain.PaymentIntent;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.commerce.domain.ProductStatus;
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

/** phase-2 切片1：混合支付购买（AC-17/18/19/23）。 */
class MixedPaymentPurchaseTest {

    private static final Instant T0 = Instant.parse("2026-09-17T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);

    private InMemoryProducts products;
    private InMemoryAccounts accounts;
    private InMemoryOrders orders;
    private InMemoryEntitlements entitlements;
    private InMemoryLedger ledger;
    private PurchaseProduct purchase;

    @BeforeEach
    void setUp() {
        products = new InMemoryProducts();
        accounts = new InMemoryAccounts();
        orders = new InMemoryOrders();
        entitlements = new InMemoryEntitlements();
        ledger = new InMemoryLedger();
        purchase = new PurchaseProduct(products, accounts, orders, entitlements, ledger, CLOCK);

        // P1 = 30.00 元 = 3000 分
        products.put(
                Product.create(
                        "P-1",
                        "ORG-1",
                        "月卡",
                        Money.cny(3_000),
                        30,
                        ProductStatus.PUBLISHED));
        accounts.put(
                Account.open(
                        "ACC-BAL",
                        AccountOwnerType.USER,
                        "U-1",
                        AccountType.BALANCE,
                        Currency.CNY,
                        10_000));
        accounts.put(
                Account.open(
                        "ACC-PTS",
                        AccountOwnerType.USER,
                        "U-1",
                        AccountType.POINTS,
                        Currency.CNY,
                        5_000,
                        T0.plusSeconds(86_400)));
        accounts.put(
                Account.open(
                        "ACC-ORG",
                        AccountOwnerType.ORG,
                        "ORG-1",
                        AccountType.SETTLEMENT,
                        Currency.CNY,
                        0));
    }

    @Test
    @DisplayName("AC-17：纯余额支付只写 ORDER_PAYMENT_BALANCE")
    void balanceOnly() {
        DomainOutcome<PurchaseResult> outcome =
                purchase.execute("U-1", "P-1", new PaymentIntent(0, 3_000));

        assertInstanceOf(DomainOutcome.Ok.class, outcome);
        List<LedgerEntry> entries = ledger.findByOrderId(
                ((DomainOutcome.Ok<PurchaseResult>) outcome).value().order().id());
        assertEquals(1, entries.size());
        assertEquals(LedgerRefType.ORDER_PAYMENT_BALANCE, entries.get(0).refType());
        assertEquals(7_000, accounts.get("ACC-BAL").balanceCents());
        assertEquals(5_000, accounts.get("ACC-PTS").balanceCents());
    }

    @Test
    @DisplayName("AC-18：混合支付拆两条分录且借贷平衡")
    void mixedPayment() {
        DomainOutcome<PurchaseResult> outcome =
                purchase.execute("U-1", "P-1", new PaymentIntent(2_000, 1_000));

        assertInstanceOf(DomainOutcome.Ok.class, outcome);
        String orderId =
                ((DomainOutcome.Ok<PurchaseResult>) outcome).value().order().id();
        List<LedgerEntry> entries = ledger.findByOrderId(orderId);
        assertEquals(2, entries.size());
        assertTrue(
                entries.stream()
                        .anyMatch(
                                e ->
                                        e.refType() == LedgerRefType.ORDER_PAYMENT_POINTS
                                                && e.amount().cents() == 2_000));
        assertTrue(
                entries.stream()
                        .anyMatch(
                                e ->
                                        e.refType() == LedgerRefType.ORDER_PAYMENT_BALANCE
                                                && e.amount().cents() == 1_000));
        LedgerInvariant.assertBalanced(ledger.findAll());
        assertEquals(9_000, accounts.get("ACC-BAL").balanceCents());
        assertEquals(3_000, accounts.get("ACC-PTS").balanceCents());
    }

    @Test
    @DisplayName("AC-19：积分不足拒绝且不分录")
    void insufficientPoints() {
        accounts.put(
                Account.open(
                        "ACC-PTS",
                        AccountOwnerType.USER,
                        "U-1",
                        AccountType.POINTS,
                        Currency.CNY,
                        500,
                        T0.plusSeconds(86_400)));

        DomainOutcome<PurchaseResult> outcome =
                purchase.execute("U-1", "P-1", new PaymentIntent(2_000, 1_000));

        assertInstanceOf(DomainOutcome.Err.class, outcome);
        assertEquals(
                DomainErrorCode.INSUFFICIENT_POINTS,
                ((DomainOutcome.Err<PurchaseResult>) outcome).code());
        assertTrue(ledger.findAll().isEmpty());
    }

    @Test
    @DisplayName("AC-23：纯积分支付")
    void pointsOnly() {
        DomainOutcome<PurchaseResult> outcome =
                purchase.execute("U-1", "P-1", new PaymentIntent(3_000, 0));

        assertInstanceOf(DomainOutcome.Ok.class, outcome);
        List<LedgerEntry> entries = ledger.findByOrderId(
                ((DomainOutcome.Ok<PurchaseResult>) outcome).value().order().id());
        assertEquals(1, entries.size());
        assertEquals(LedgerRefType.ORDER_PAYMENT_POINTS, entries.get(0).refType());
        assertEquals(10_000, accounts.get("ACC-BAL").balanceCents());
        assertEquals(2_000, accounts.get("ACC-PTS").balanceCents());
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
        public Account findUserBalance(String userId, Currency currency) {
            return byId.values().stream()
                    .filter(a -> a.type() == AccountType.BALANCE && a.ownerId().equals(userId))
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public Account findUserPoints(String userId, Currency currency) {
            return byId.values().stream()
                    .filter(a -> a.type() == AccountType.POINTS && a.ownerId().equals(userId))
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public Account findOrgSettlement(String orgId, Currency currency) {
            return byId.values().stream()
                    .filter(a -> a.type() == AccountType.SETTLEMENT && a.ownerId().equals(orgId))
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public void save(Account account) {
            byId.put(account.id(), account);
        }
    }

    private static final class InMemoryOrders implements OrderRepository {
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

    private static final class InMemoryEntitlements implements EntitlementRepository {
        private final List<Entitlement> saved = new ArrayList<>();

        @Override
        public void save(Entitlement entitlement) {
            saved.add(entitlement);
        }

        @Override
        public Entitlement get(String entitlementId) {
            return saved.stream().filter(e -> e.id().equals(entitlementId)).findFirst().orElseThrow();
        }

        @Override
        public Optional<Entitlement> findByOrderId(String orderId) {
            return saved.stream().filter(e -> e.orderId().equals(orderId)).findFirst();
        }

        @Override
        public List<Entitlement> findActiveByUser(String userId) {
            return saved.stream()
                    .filter(e -> e.userId().equals(userId) && e.status() == EntitlementStatus.ACTIVE)
                    .toList();
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
