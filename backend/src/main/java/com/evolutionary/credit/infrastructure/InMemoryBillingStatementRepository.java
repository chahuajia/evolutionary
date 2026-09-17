package com.evolutionary.credit.infrastructure;

import com.evolutionary.credit.application.BillingStatementRepository;
import com.evolutionary.credit.domain.BillingStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryBillingStatementRepository implements BillingStatementRepository {

    private final Map<String, BillingStatement> byId = new ConcurrentHashMap<>();

    @Override
    public void save(BillingStatement statement) {
        byId.put(statement.id(), statement);
    }

    @Override
    public Optional<BillingStatement> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<BillingStatement> findByUserId(String userId) {
        List<BillingStatement> out = new ArrayList<>();
        for (BillingStatement s : byId.values()) {
            if (s.userId().equals(userId)) {
                out.add(s);
            }
        }
        out.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
        return out;
    }
}
