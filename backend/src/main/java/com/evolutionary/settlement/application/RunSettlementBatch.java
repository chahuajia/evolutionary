package com.evolutionary.settlement.application;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.settlement.domain.AccrualStatus;
import com.evolutionary.settlement.domain.ProfitShareAccrual;
import com.evolutionary.settlement.domain.SettlementBatch;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 显式跑结算批（规格 T+7 的简化入口：由调用方给定 period）。
 *
 * <p>流程：收集 PENDING → 按 org 汇总写 {@code PROFIT_SHARING_SETTLEMENT} → Accrual SETTLED → Batch
 * CLOSED。REVERSED 不会被查出（INV-15）。
 */
public final class RunSettlementBatch {

    private final ProfitShareAccrualRepository accruals;
    private final SettlementBatchRepository batches;
    private final LedgerRepository ledger;
    private final AccountRepository accounts;
    /** 清算户：SETTLEMENT 户间划转的借方。 */
    private final String clearingAccountId;

    public RunSettlementBatch(
            ProfitShareAccrualRepository accruals,
            SettlementBatchRepository batches,
            LedgerRepository ledger,
            AccountRepository accounts,
            String clearingAccountId) {
        this.accruals = Objects.requireNonNull(accruals, "accruals");
        this.batches = Objects.requireNonNull(batches, "batches");
        this.ledger = Objects.requireNonNull(ledger, "ledger");
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        if (clearingAccountId == null || clearingAccountId.isBlank()) {
            throw new IllegalArgumentException("clearingAccountId 不能为空");
        }
        this.clearingAccountId = clearingAccountId;
    }

    /**
     * @param periodStart 含
     * @param periodEnd 不含
     */
    public SettlementBatch execute(Instant periodStart, Instant periodEnd, Instant runAt) {
        Objects.requireNonNull(periodStart, "periodStart");
        Objects.requireNonNull(periodEnd, "periodEnd");
        Objects.requireNonNull(runAt, "runAt");

        SettlementBatch open =
                SettlementBatch.open(UUID.randomUUID().toString(), periodStart, periodEnd, runAt);
        batches.save(open);

        List<ProfitShareAccrual> pending =
                accruals.findPendingCreatedBetween(periodStart, periodEnd).stream()
                        .filter(a -> a.status() == AccrualStatus.PENDING)
                        .toList();

        Map<String, Long> byOrg =
                pending.stream()
                        .collect(
                                Collectors.groupingBy(
                                        ProfitShareAccrual::orgId,
                                        LinkedHashMap::new,
                                        Collectors.summingLong(ProfitShareAccrual::amountCents)));

        String currencyCode =
                pending.isEmpty() ? Currency.CNY.name() : pending.get(0).currency();
        Currency currency = Currency.valueOf(currencyCode);

        Account clearing = accounts.get(clearingAccountId);
        for (Map.Entry<String, Long> e : byOrg.entrySet()) {
            long amount = e.getValue();
            if (amount <= 0) {
                continue;
            }
            Account orgSettlement = accounts.findOrgSettlement(e.getKey(), currency);
            clearing = clearing.debit(amount);
            Account credited = orgSettlement.credit(amount);
            ledger.append(
                    LedgerEntry.profitSharingSettlement(
                            UUID.randomUUID().toString(),
                            clearingAccountId,
                            orgSettlement.id(),
                            new Money(amount, currency),
                            open.id(),
                            runAt));
            accounts.save(credited);
        }
        accounts.save(clearing);

        List<ProfitShareAccrual> settled = new ArrayList<>(pending.size());
        for (ProfitShareAccrual a : pending) {
            settled.add(a.settle(open.id(), runAt));
        }
        accruals.saveAll(settled);

        SettlementBatch closed = open.close(runAt);
        batches.save(closed);
        return closed;
    }
}
