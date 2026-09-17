package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.LedgerEntry;
import java.util.List;

public interface LedgerRepository {
    void append(LedgerEntry entry);

    List<LedgerEntry> findAll();
}
