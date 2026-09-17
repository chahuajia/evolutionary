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
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.commerce.domain.OrderStatus;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.commerce.domain.ProductStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PurchaseProductTest {

    private static final Instant T0 = Instant.parse("2026-09-17T00:00:00Z");
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
        purchase =
                new PurchaseProduct(products, accounts, orders, entitlements, ledger, CLOCK);

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
    @DisplayName("余额足够时购买成功并生成权益")
    void successfulPurchase() {
        DomainOutcome<PurchaseResult> outcome = purchase.execute("U-1", "P-1");

        assertInstanceOf(DomainOutcome.Ok.class, outcome);
        PurchaseResult result = ((DomainOutcome.Ok<PurchaseResult>) outcome).value();

        assertEquals(OrderStatus.PAID, result.order().status());
        assertEquals(EntitlementStatus.ACTIVE, result.entitlement().status());
        assertEquals(10_100, accounts.get("ACC-U-1").balanceCents());
        assertEquals(9_900, accounts.get("ACC-O-1").balanceCents());
        LedgerInvariant.assertBalanced(ledger.findAll());
        assertTrue(result.entitlement().isActiveAt(T0));
    }

    @Test
    @DisplayName("余额不足时拒绝")
    void insufficientBalance() {
        accounts.put(
                Account.open(
                        "ACC-U-1",
                        AccountOwnerType.USER,
                        "U-1",
                        AccountType.BALANCE,
                        Currency.CNY,
                        100));

        DomainOutcome<PurchaseResult> outcome = purchase.execute("U-1", "P-1");

        assertInstanceOf(DomainOutcome.Err.class, outcome);
        DomainOutcome.Err<PurchaseResult> err = (DomainOutcome.Err<PurchaseResult>) outcome;
        assertEquals(DomainErrorCode.INSUFFICIENT_BALANCE, err.code());
    }

    @Test
    @DisplayName("未发布商品拒绝购买")
    void unpublishedProduct() {
        products.put(
                Product.create(
                        "P-2",
                        "ORG-1",
                        "草稿卡",
                        Money.cny(100),
                        30,
                        ProductStatus.DRAFT));

        DomainOutcome<PurchaseResult> outcome = purchase.execute("U-1", "P-2");

        assertInstanceOf(DomainOutcome.Err.class, outcome);
        assertEquals(
                DomainErrorCode.PRODUCT_NOT_PUBLISHED,
                ((DomainOutcome.Err<PurchaseResult>) outcome).code());
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
        private final List<Order> saved = new ArrayList<>();

        @Override
        public void save(Order order) {
            saved.add(order);
        }
    }

    private static final class InMemoryEntitlements implements EntitlementRepository {
        private final List<Entitlement> saved = new ArrayList<>();

        @Override
        public void save(Entitlement entitlement) {
            saved.add(entitlement);
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
