package com.evolutionary.credit.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.application.OrderRepository;
import com.evolutionary.commerce.application.ProductRepository;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerRefType;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.credit.domain.CreditErrorCode;
import com.evolutionary.credit.domain.CreditLedgerDebt;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.credit.domain.ScoreTier;
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

/** AC-48 / AC-49 · INV-17。 */
class PurchaseWithCreditTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);

    private InMemoryProducts products;
    private InMemoryOrders orders;
    private InMemoryEntitlements entitlements;
    private InMemoryLedger ledger;
    private InMemoryProfiles profiles;
    private InMemoryDebts debts;
    private PurchaseWithCredit purchase;

    @BeforeEach
    void setUp() {
        products = new InMemoryProducts();
        orders = new InMemoryOrders();
        entitlements = new InMemoryEntitlements();
        ledger = new InMemoryLedger();
        profiles = new InMemoryProfiles();
        debts = new InMemoryDebts();
        purchase =
                new PurchaseWithCredit(
                        products, orders, entitlements, ledger, profiles, debts, CLOCK);

        products.put(
                Product.create(
                        "P1",
                        "ORG-1",
                        "月卡",
                        Money.cny(3_000),
                        30,
                        Product.Status.PUBLISHED));
        profiles.save(CreditProfile.open("U1", Money.cny(10_000), ScoreTier.A, 1));
    }

    @Test
    @DisplayName("AC-48：余额 0 信用购 → PAID + Debt OPEN + usedCredit + Entitlement，无余额支付分录")
    void creditPurchaseWithoutBalanceLedger() {
        CreditOutcome<CreditPurchaseResult> outcome = purchase.execute("U1", "P1");
        assertInstanceOf(CreditOutcome.Ok.class, outcome);
        CreditPurchaseResult result = ((CreditOutcome.Ok<CreditPurchaseResult>) outcome).value();

        assertEquals(Order.Status.PAID, result.order().status());
        assertEquals(CreditLedgerDebt.Status.OPEN, result.debt().status());
        assertEquals(3_000, result.debt().amount().cents());
        assertEquals(3_000, result.profile().usedCredit().cents());
        assertEquals(Entitlement.Status.ACTIVE, result.entitlement().status());
        assertTrue(
                ledger.findByOrderId(result.order().id()).stream()
                        .noneMatch(e -> e.refType() == LedgerRefType.ORDER_PAYMENT_BALANCE));
        assertTrue(ledger.findAll().isEmpty());
    }

    @Test
    @DisplayName("AC-49：额度不足 → CREDIT_LIMIT_EXCEEDED，无 PAID 订单")
    void creditLimitExceeded() {
        profiles.save(CreditProfile.open("U1", Money.cny(1_000), ScoreTier.B, 1));

        CreditOutcome<CreditPurchaseResult> outcome = purchase.execute("U1", "P1");
        assertInstanceOf(CreditOutcome.Err.class, outcome);
        assertEquals(
                CreditErrorCode.CREDIT_LIMIT_EXCEEDED,
                ((CreditOutcome.Err<CreditPurchaseResult>) outcome).code());
        assertTrue(orders.isEmpty());
        assertTrue(debts.isEmpty());
        assertEquals(0, profiles.get("U1").usedCredit().cents());
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
            return Optional.ofNullable(byId.get(productId)).orElseThrow();
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
            return Optional.ofNullable(byId.get(orderId)).orElseThrow();
        }

        boolean isEmpty() {
            return byId.isEmpty();
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
            return Optional.ofNullable(byId.get(entitlementId)).orElseThrow();
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

        boolean isEmpty() {
            return byId.isEmpty();
        }
    }
}
