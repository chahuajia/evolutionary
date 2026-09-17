package com.evolutionary.commerce.domain;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** INV-1：分录集合按币种借贷平衡。 */
public final class LedgerInvariant {

    private LedgerInvariant() {}

    public static void assertBalanced(List<LedgerEntry> entries) {
        Map<Currency, Long> debits = new EnumMap<>(Currency.class);
        Map<Currency, Long> credits = new EnumMap<>(Currency.class);
        for (LedgerEntry entry : entries) {
            Money amount = entry.amount();
            debits.merge(amount.currency(), amount.cents(), Long::sum);
            credits.merge(amount.currency(), amount.cents(), Long::sum);
        }
        for (Currency currency : Currency.values()) {
            long debitTotal = debits.getOrDefault(currency, 0L);
            long creditTotal = credits.getOrDefault(currency, 0L);
            if (debitTotal != creditTotal) {
                throw new UnbalancedLedgerException(currency, debitTotal, creditTotal);
            }
        }
    }

    public static final class UnbalancedLedgerException extends RuntimeException {
        UnbalancedLedgerException(Currency currency, long debits, long credits) {
            super("ledger unbalanced for " + currency + ": debits=" + debits + " credits=" + credits);
        }
    }
}
