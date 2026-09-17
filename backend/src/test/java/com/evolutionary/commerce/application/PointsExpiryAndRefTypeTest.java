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

/** phase-2 切片3：积分过期（AC-21）与 refType 不混用（AC-22）。 */
class PointsExpiryAndRefTypeTest {

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

        products.put(
                Product.create(
                        "P-1", "ORG-1", "月卡", Money.cny(3_000), 30, ProductStatus.PUBLISHED));
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
                        "ACC-ORG",
                        AccountOwnerType.ORG,
                        "ORG-1",
                        AccountType.SETTLEMENT,
                        Currency.CNY,
                        0));
    }

    @Test
    @DisplayName("AC-21：积分已过期时拒绝 POINTS_EXPIRED")
    void expiredPointsRejected() {
        accounts.put(
                Account.open(
                        "ACC-PTS",
                        AccountOwnerType.USER,
                        "U-1",
                        AccountType.POINTS,
                        Currency.CNY,
                        5_000,
                        T0.minusSeconds(86_400)));

        DomainOutcome<PurchaseResult> outcome =
                purchase.execute("U-1", "P-1", new PaymentIntent(2_000, 1_000));

        assertInstanceOf(DomainOutcome.Err.class, outcome);
        assertEquals(
                DomainErrorCode.POINTS_EXPIRED,
                ((DomainOutcome.Err<PurchaseResult>) outcome).code());
        assertTrue(ledger.findAll().isEmpty());
    }

    @Test
    @DisplayName("AC-22：POINTS 分录不扣 BALANCE 账户，BALANCE 分录不扣 POINTS 账户")
    void refTypeMatchesAccount() {
        accounts.put(
                Account.open(
                        "ACC-PTS",
                        AccountOwnerType.USER,
                        "U-1",
                        AccountType.POINTS,
                        Currency.CNY,
                        5_000,
                        T0.plusSeconds(86_400)));

        DomainOutcome<PurchaseResult> outcome =
                purchase.execute("U-1", "P-1", new PaymentIntent(2_000, 1_000));
        assertInstanceOf(DomainOutcome.Ok.class, outcome);

        for (LedgerEntry e : ledger.findAll()) {
            Account debit = accounts.get(e.debitAccountId());
            if (e.refType() == LedgerRefType.ORDER_PAYMENT_POINTS) {
                assertEquals(AccountType.POINTS, debit.type());
            }
            if (e.refType() == LedgerRefType.ORDER_PAYMENT_BALANCE) {
                assertEquals(AccountType.BALANCE, debit.type());
            }
        }
    }

    private static final class InMemoryProducts implements ProductRepository {
        private final Map<String, Product> byId = new HashMap<>();

        void put(Product p) {
            byId.put(p.id(), p);
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

        @Override
        public List<LedgerEntry> findByRefId(String refId) {
            return findByOrderId(refId);
        }
    }
}
