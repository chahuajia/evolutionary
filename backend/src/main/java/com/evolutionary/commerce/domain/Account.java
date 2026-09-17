package com.evolutionary.commerce.domain;

import java.util.Objects;

public final class Account {

    private final String id;
    private final AccountOwnerType ownerType;
    private final String ownerId;
    private final AccountType type;
    private final Currency currency;
    private final long balanceCents;

    private Account(
            String id,
            AccountOwnerType ownerType,
            String ownerId,
            AccountType type,
            Currency currency,
            long balanceCents) {
        this.id = id;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.type = type;
        this.currency = currency;
        this.balanceCents = balanceCents;
    }

    public static Account open(
            String id,
            AccountOwnerType ownerType,
            String ownerId,
            AccountType type,
            Currency currency,
            long openingBalanceCents) {
        if (openingBalanceCents < 0) {
            throw new IllegalArgumentException("opening balance must not be negative");
        }
        return new Account(
                requireId(id),
                Objects.requireNonNull(ownerType, "ownerType"),
                requireId(ownerId),
                Objects.requireNonNull(type, "type"),
                Objects.requireNonNull(currency, "currency"),
                openingBalanceCents);
    }

    public Account debit(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        if (balanceCents < amount) {
            throw new InsufficientBalanceException();
        }
        return new Account(id, ownerType, ownerId, type, currency, balanceCents - amount);
    }

    public Account credit(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        return new Account(id, ownerType, ownerId, type, currency, balanceCents + amount);
    }

    public boolean canCover(Money price) {
        return currency == price.currency() && balanceCents >= price.cents();
    }

    public String id() {
        return id;
    }

    public AccountOwnerType ownerType() {
        return ownerType;
    }

    public String ownerId() {
        return ownerId;
    }

    public AccountType type() {
        return type;
    }

    public Currency currency() {
        return currency;
    }

    public long balanceCents() {
        return balanceCents;
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }

    public static final class InsufficientBalanceException extends RuntimeException {}
}
