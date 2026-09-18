package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import org.springframework.stereotype.Component;

/** 账户 JPA 适配。 */
@Component
public final class JpaAccountRepository implements AccountRepository {

    private final AccountJpaRepository jpa;

    public JpaAccountRepository(AccountJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Account get(String accountId) {
        return jpa.findById(accountId)
                .map(JpaAccountRepository::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("unknown account"));
    }

    @Override
    public Account findUserBalance(String userId, Currency currency) {
        return jpa.findFirstByOwnerTypeAndOwnerIdAndTypeAndCurrency(
                        AccountOwnerType.USER, userId, AccountType.BALANCE, currency)
                .map(JpaAccountRepository::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("user balance not found"));
    }

    @Override
    public Account findUserPoints(String userId, Currency currency) {
        return jpa.findFirstByOwnerTypeAndOwnerIdAndTypeAndCurrency(
                        AccountOwnerType.USER, userId, AccountType.POINTS, currency)
                .map(JpaAccountRepository::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("user points not found"));
    }

    @Override
    public Account findOrgSettlement(String orgId, Currency currency) {
        return jpa.findFirstByOwnerTypeAndOwnerIdAndTypeAndCurrency(
                        AccountOwnerType.ORG, orgId, AccountType.SETTLEMENT, currency)
                .map(JpaAccountRepository::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("org settlement not found"));
    }

    @Override
    public void save(Account account) {
        jpa.save(
                new AccountJpaEntity(
                        account.id(),
                        account.ownerType(),
                        account.ownerId(),
                        account.type(),
                        account.currency(),
                        account.balanceCents(),
                        account.pointsExpiresAt()));
    }

    private static Account toDomain(AccountJpaEntity row) {
        return Account.rehydrate(
                row.getId(),
                row.getOwnerType(),
                row.getOwnerId(),
                row.getType(),
                row.getCurrency(),
                row.getBalanceCents(),
                row.getPointsExpiresAt());
    }
}
