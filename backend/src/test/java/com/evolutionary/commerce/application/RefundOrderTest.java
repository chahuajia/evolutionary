package com.evolutionary.commerce.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
import com.evolutionary.commerce.domain.OrderStatus;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.commerce.domain.ProductStatus;
import com.evolutionary.commerce.domain.UsageEvent;
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

class RefundOrderTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);

    private InMemoryProducts products;
    private InMemoryAccounts accounts;
    private InMemoryOrders orders;
    private InMemoryEntitlements entitlements;
    private InMemoryUsages usages;
    private InMemoryLedger ledger;
    private PurchaseProduct purchase;
    private RefundOrder refund;

    @BeforeEach
    void setUp() {
        products = new InMemoryProducts();
        accounts = new InMemoryAccounts();
        orders = new InMemoryOrders();
        entitlements = new InMemoryEntitlements();
        usages = new InMemoryUsages();
        ledger = new InMemoryLedger();
        purchase =
                new PurchaseProduct(products, accounts, orders, entitlements, ledger, CLOCK);
        refund = new RefundOrder(orders, entitlements, usages, accounts, ledger, CLOCK);

        products.put(
                Product.create(
                        "P-1",
                        "ORG-1",
                        "30天不限次换电卡",
                        Money.cny(9_900),
                        30,
                        ProductStatus.PUBLISHED));
        accounts.put(
                Account.open(
                        "ACC-U-1",
                        AccountOwnerType.USER,
                        "U-1",
                        AccountType.BALANCE,
                        Currency.CNY,
                        20_000));
        accounts.put(
                Account.open(
                        "ACC-O-1",
                        AccountOwnerType.ORG,
                        "ORG-1",
                        AccountType.SETTLEMENT,
                        Currency.CNY,
                        0));
    }

    @Test
    @DisplayName("PAID 订单退款成功：Order REFUNDED、权益 REVOKED、反向分录")
    void successfulRefund() {
        PurchaseResult bought =
                ((DomainOutcome.Ok<PurchaseResult>) purchase.execute("U-1", "P-1")).value();

        DomainOutcome<RefundResult> outcome = refund.execute(bought.order().id());

        assertInstanceOf(DomainOutcome.Ok.class, outcome);
        RefundResult result = ((DomainOutcome.Ok<RefundResult>) outcome).value();
        assertEquals(OrderStatus.REFUNDED, result.order().status());
        assertEquals(EntitlementStatus.REVOKED, result.entitlement().status());
        assertEquals(20_000, accounts.get("ACC-U-1").balanceCents());
        assertEquals(0, accounts.get("ACC-O-1").balanceCents());
        LedgerInvariant.assertBalanced(ledger.findAll());
        assertEquals(
                1,
                ledger.findAll().stream()
                        .filter(e -> e.refType() == LedgerRefType.ORDER_REFUND)
                        .count());
    }

    @Test
    @DisplayName("有 STARTED 换电时拒绝退款（Q1）")
    void blocksWhenSwapInProgress() {
        PurchaseResult bought =
                ((DomainOutcome.Ok<PurchaseResult>) purchase.execute("U-1", "P-1")).value();
        usages.save(
                UsageEvent.start(
                        "UE-1",
                        "U-1",
                        bought.entitlement().id(),
                        "BAT-1",
                        "CAB-1",
                        T0));

        DomainOutcome<RefundResult> outcome = refund.execute(bought.order().id());

        assertInstanceOf(DomainOutcome.Err.class, outcome);
        assertEquals(
                DomainErrorCode.REFUND_BLOCKED_IN_PROGRESS_SWAP,
                ((DomainOutcome.Err<RefundResult>) outcome).code());
        assertEquals(OrderStatus.PAID, orders.get(bought.order().id()).status());
        assertEquals(EntitlementStatus.ACTIVE, entitlements.get(bought.entitlement().id()).status());
    }

    @Test
    @DisplayName("非 PAID 订单拒绝退款")
    void rejectsNonPaidOrder() {
        Order created =
                Order.create("O-x", "U-1", "P-1", "ORG-1", Money.cny(9_900), T0);
        orders.save(created);
        entitlements.save(
                Entitlement.rehydrate(
                        "E-x",
                        "O-x",
                        "U-1",
                        "P-1",
                        T0,
                        T0.plusSeconds(86_400),
                        EntitlementStatus.ACTIVE));

        DomainOutcome<RefundResult> outcome = refund.execute("O-x");

        assertInstanceOf(DomainOutcome.Err.class, outcome);
        assertEquals(
                DomainErrorCode.ORDER_NOT_REFUNDABLE,
                ((DomainOutcome.Err<RefundResult>) outcome).code());
    }

    private static final class InMemoryProducts implements ProductRepository {
        private final Map<String, Product> byId = new HashMap<>();

        void put(Product product) {
            byId.put(product.id(), product);
        }

        @Override
        public Product get(String productId) {
            Product product = byId.get(productId);
            if (product == null) {
                throw new IllegalArgumentException("unknown product: " + productId);
            }
            return product;
        }
    }

    private static final class InMemoryAccounts implements AccountRepository {
        private final Map<String, Account> byId = new HashMap<>();

        void put(Account account) {
            byId.put(account.id(), account);
        }

        @Override
        public Account get(String accountId) {
            return byId.get(accountId);
        }

        @Override
        public Account findUserBalance(String userId, Currency currency) {
            return byId.values().stream()
                    .filter(
                            a ->
                                    a.ownerType() == AccountOwnerType.USER
                                            && a.ownerId().equals(userId)
                                            && a.type() == AccountType.BALANCE
                                            && a.currency() == currency)
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public Account findOrgSettlement(String orgId, Currency currency) {
            return byId.values().stream()
                    .filter(
                            a ->
                                    a.ownerType() == AccountOwnerType.ORG
                                            && a.ownerId().equals(orgId)
                                            && a.type() == AccountType.SETTLEMENT
                                            && a.currency() == currency)
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
            Order order = byId.get(orderId);
            if (order == null) {
                throw new IllegalArgumentException("unknown order: " + orderId);
            }
            return order;
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
            Entitlement e = byId.get(entitlementId);
            if (e == null) {
                throw new IllegalArgumentException("unknown entitlement");
            }
            return e;
        }

        @Override
        public Optional<Entitlement> findByOrderId(String orderId) {
            return byId.values().stream().filter(e -> e.orderId().equals(orderId)).findFirst();
        }

        @Override
        public List<Entitlement> findActiveByUser(String userId) {
            return byId.values().stream()
                    .filter(e -> e.userId().equals(userId))
                    .filter(e -> e.status() == EntitlementStatus.ACTIVE)
                    .toList();
        }
    }

    private static final class InMemoryUsages implements UsageEventRepository {
        private final List<UsageEvent> events = new ArrayList<>();

        @Override
        public void save(UsageEvent event) {
            events.removeIf(e -> e.id().equals(event.id()));
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
    }
}
