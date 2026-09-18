package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内账户仓储（单测用；生产由 {@link JpaAccountRepository} 接管）。 */
public final class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> byId = new ConcurrentHashMap<>();

    @Override
    public Account get(String accountId) {
        Account a = byId.get(accountId);
        if (a == null) {
            throw new IllegalArgumentException("unknown account");
        }
        return a;
    }

    @Override
    public void save(Account account) {
        byId.put(account.id(), account);
    }

    @Override
    public Account findUserBalance(String userId, Currency currency) {
        return byId.values().stream()
                .filter(a -> a.ownerType() == AccountOwnerType.USER)
                .filter(a -> a.ownerId().equals(userId))
                .filter(a -> a.type() == AccountType.BALANCE)
                .filter(a -> a.currency() == currency)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("user balance not found"));
    }

    @Override
    public Account findUserPoints(String userId, Currency currency) {
        return byId.values().stream()
                .filter(a -> a.ownerType() == AccountOwnerType.USER)
                .filter(a -> a.ownerId().equals(userId))
                .filter(a -> a.type() == AccountType.POINTS)
                .filter(a -> a.currency() == currency)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("user points not found"));
    }

    @Override
    public Account findOrgSettlement(String orgId, Currency currency) {
        return byId.values().stream()
                .filter(a -> a.ownerType() == AccountOwnerType.ORG)
                .filter(a -> a.ownerId().equals(orgId))
                .filter(a -> a.type() == AccountType.SETTLEMENT)
                .filter(a -> a.currency() == currency)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("org settlement not found"));
    }
}
