package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.Money;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 分录 JPA 适配。 */
@Component
public final class JpaLedgerRepository implements LedgerRepository {

    private final LedgerEntryJpaRepository jpa;

    public JpaLedgerRepository(LedgerEntryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void append(LedgerEntry entry) {
        jpa.save(
                new LedgerEntryJpaEntity(
                        entry.id(),
                        entry.debitAccountId(),
                        entry.creditAccountId(),
                        entry.amount().cents(),
                        entry.amount().currency(),
                        entry.refType(),
                        entry.refId(),
                        entry.createdAt()));
    }

    @Override
    public List<LedgerEntry> findAll() {
        List<LedgerEntry> result = new ArrayList<>();
        for (LedgerEntryJpaEntity row : jpa.findAll()) {
            result.add(toDomain(row));
        }
        return result;
    }

    @Override
    public List<LedgerEntry> findByOrderId(String orderId) {
        List<LedgerEntry> result = new ArrayList<>();
        for (LedgerEntryJpaEntity row : jpa.findByRefIdOrderByCreatedAtAsc(orderId)) {
            result.add(toDomain(row));
        }
        return result;
    }

    private static LedgerEntry toDomain(LedgerEntryJpaEntity row) {
        return LedgerEntry.rehydrate(
                row.getId(),
                row.getDebitAccountId(),
                row.getCreditAccountId(),
                new Money(row.getAmountCents(), row.getAmountCurrency()),
                row.getRefType(),
                row.getRefId(),
                row.getCreatedAt());
    }
}
