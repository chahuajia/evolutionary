package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.LedgerEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 进程内分录仓储（还款路径最小实现）。 */
public final class InMemoryLedgerRepository implements LedgerRepository {

    private final List<LedgerEntry> entries = new CopyOnWriteArrayList<>();

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
