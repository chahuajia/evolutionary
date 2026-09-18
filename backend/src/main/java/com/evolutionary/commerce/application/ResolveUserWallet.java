package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.Currency;
import java.util.Objects;
import java.util.Optional;

/**
 * 解析用户钱包读模型（切片27b）。
 *
 * <p>复用 {@link AccountRepository#findUserBalance} / {@link AccountRepository#findUserPoints}；
 * 任一账户缺失 → empty（HTTP 404）。
 */
public final class ResolveUserWallet {

    private final AccountRepository accounts;
    private final Currency currency;

    public ResolveUserWallet(AccountRepository accounts) {
        this(accounts, Currency.CNY);
    }

    public ResolveUserWallet(AccountRepository accounts, Currency currency) {
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    public Optional<UserWallet> execute(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        String id = userId.trim();
        try {
            Account balance = accounts.findUserBalance(id, currency);
            Account points = accounts.findUserPoints(id, currency);
            return Optional.of(
                    new UserWallet(
                            id, balance.balanceCents(), points.balanceCents(), currency));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
