package com.evolutionary.settlement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerRefType;
import com.evolutionary.settlement.domain.AccrualStatus;
import com.evolutionary.settlement.domain.BatchStatus;
import com.evolutionary.settlement.domain.ProfitShareAccrual;
import com.evolutionary.settlement.domain.ProfitSharingRule;
import com.evolutionary.settlement.domain.SettlementBatch;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AC-34：显式 runBatch 结算 PENDING → Ledger + SETTLED（INV-14）。 */
class RunSettlementBatchTest {

    private static final Instant T0 = Instant.parse("2026-09-17T12:00:00Z");
    private static final Instant T7 = T0.plusSeconds(7L * 24 * 3600);
    private static final String L1 = "ORG-L1";
    private static final String L2 = "ORG-L2";
    private static final String CLEARING_ID = "acc-clearing";

    private InMemoryAccrualRepo accruals;
    private InMemoryBatchRepo batches;
    private InMemoryLedger ledger;
    private InMemoryAccounts accounts;
    private RunSettlementBatch runBatch;

    @BeforeEach
    void setUp() {
        accruals = new InMemoryAccrualRepo();
        batches = new InMemoryBatchRepo();
        ledger = new InMemoryLedger();
        accounts = new InMemoryAccounts();
        accounts.put(
                Account.open(
                        CLEARING_ID,
                        AccountOwnerType.PLATFORM,
                        "CLEARING",
                        AccountType.SETTLEMENT,
                        Currency.CNY,
                        1_000_000));
        for (String org : List.of(L1, L2, ProfitSharingRule.PLATFORM_ORG_ID)) {
            accounts.put(
                    Account.open(
                            "stl-" + org,
                            AccountOwnerType.ORG,
                            org,
                            AccountType.SETTLEMENT,
                            Currency.CNY,
                            0));
        }
        runBatch = new RunSettlementBatch(accruals, batches, ledger, accounts, CLEARING_ID);
    }

    @Test
    @DisplayName("AC-34：PENDING 在 T+7 runBatch 后 SETTLED，并按 org 写 PROFIT_SHARING_SETTLEMENT")
    void settlePendingWritesLedgerPerOrg() {
        seedPending("O1", T0, Map.of(L2, 1_000L, L1, 500L, ProfitSharingRule.PLATFORM_ORG_ID, 8_500L));

        SettlementBatch batch = runBatch.execute(T0, T7, T7);

        assertEquals(BatchStatus.CLOSED, batch.status());
        assertTrue(
                accruals.findByOrderId("O1").stream()
                        .allMatch(a -> a.status() == AccrualStatus.SETTLED));
        assertTrue(
                accruals.findByOrderId("O1").stream()
                        .allMatch(a -> batch.id().equals(a.batchId())));

        List<LedgerEntry> settlementEntries =
                ledger.findAll().stream()
                        .filter(e -> e.refType() == LedgerRefType.PROFIT_SHARING_SETTLEMENT)
                        .filter(e -> e.refId().equals(batch.id()))
                        .toList();
        assertEquals(3, settlementEntries.size());

        Map<String, Long> ledgerByOrg =
                settlementEntries.stream()
                        .collect(
                                Collectors.toMap(
                                        e -> orgIdOfCredit(e.creditAccountId()),
                                        e -> e.amount().cents()));
        Map<String, Long> accrualByOrg =
                accruals.findByOrderId("O1").stream()
                        .collect(
                                Collectors.toMap(
                                        ProfitShareAccrual::orgId, ProfitShareAccrual::amountCents));
        assertEquals(accrualByOrg, ledgerByOrg); // INV-14
        assertEquals(
                10_000L, ledgerByOrg.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    @DisplayName("INV-15：REVERSED 不进 Batch")
    void reversedExcludedFromBatch() {
        seedPending("O1", T0, Map.of(L2, 1_000L, L1, 500L, ProfitSharingRule.PLATFORM_ORG_ID, 8_500L));
        for (ProfitShareAccrual a : accruals.findByOrderId("O1")) {
            accruals.save(a.reverse());
        }
        seedPending("O2", T0, Map.of(L2, 200L));

        SettlementBatch batch = runBatch.execute(T0, T7, T7);

        assertTrue(
                accruals.findByOrderId("O1").stream()
                        .noneMatch(a -> a.status() == AccrualStatus.SETTLED));
        assertTrue(
                accruals.findByOrderId("O2").stream()
                        .allMatch(
                                a ->
                                        a.status() == AccrualStatus.SETTLED
                                                && batch.id().equals(a.batchId())));
        long ledgerTotal =
                ledger.findAll().stream()
                        .filter(e -> e.refId().equals(batch.id()))
                        .mapToLong(e -> e.amount().cents())
                        .sum();
        assertEquals(200L, ledgerTotal);
    }

    private void seedPending(String orderId, Instant at, Map<String, Long> amounts) {
        List<ProfitShareAccrual> list = new ArrayList<>();
        amounts.forEach(
                (org, cents) ->
                        list.add(
                                ProfitShareAccrual.pending(
                                        orderId + "-" + org,
                                        orderId,
                                        org,
                                        cents,
                                        "CNY",
                                        1,
                                        at)));
        accruals.saveAll(list);
    }

    private static String orgIdOfCredit(String creditAccountId) {
        return creditAccountId.startsWith("stl-")
                ? creditAccountId.substring(4)
                : creditAccountId;
    }

    private static final class InMemoryAccrualRepo implements ProfitShareAccrualRepository {
        private final Map<String, ProfitShareAccrual> byId = new HashMap<>();

        @Override
        public void save(ProfitShareAccrual accrual) {
            byId.put(accrual.id(), accrual);
        }

        @Override
        public void saveAll(List<ProfitShareAccrual> items) {
            items.forEach(this::save);
        }

        @Override
        public List<ProfitShareAccrual> findByOrderId(String orderId) {
            return byId.values().stream().filter(a -> a.orderId().equals(orderId)).toList();
        }

        @Override
        public List<ProfitShareAccrual> findPendingCreatedBetween(
                Instant periodStart, Instant periodEnd) {
            return byId.values().stream()
                    .filter(a -> a.status() == AccrualStatus.PENDING)
                    .filter(
                            a ->
                                    !a.createdAt().isBefore(periodStart)
                                            && a.createdAt().isBefore(periodEnd))
                    .toList();
        }

        @Override
        public List<ProfitShareAccrual> findAll() {
            return List.copyOf(byId.values());
        }
    }

    private static final class InMemoryBatchRepo implements SettlementBatchRepository {
        private final Map<String, SettlementBatch> byId = new HashMap<>();

        @Override
        public void save(SettlementBatch batch) {
            byId.put(batch.id(), batch);
        }

        @Override
        public SettlementBatch get(String batchId) {
            SettlementBatch b = byId.get(batchId);
            if (b == null) {
                throw new IllegalArgumentException("未知批: " + batchId);
            }
            return b;
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
            throw new UnsupportedOperationException();
        }

        @Override
        public Account findUserPoints(String userId, Currency currency) {
            throw new UnsupportedOperationException();
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
}
