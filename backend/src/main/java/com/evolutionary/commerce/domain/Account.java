package com.evolutionary.commerce.domain;

import java.time.Instant;
import java.util.Objects;

/** 账户。POINTS 类型可带过期时间（阶段 2 简化：整户一个 expiresAt）。 */
public final class Account {

    private final String id;
    private final AccountOwnerType ownerType;
    private final String ownerId;
    private final AccountType type;
    private final Currency currency;
    private final long balanceCents;
    private final Instant pointsExpiresAt;

    private Account(
            String id,
            AccountOwnerType ownerType,
            String ownerId,
            AccountType type,
            Currency currency,
            long balanceCents,
            Instant pointsExpiresAt) {
        this.id = id;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.type = type;
        this.currency = currency;
        this.balanceCents = balanceCents;
        this.pointsExpiresAt = pointsExpiresAt;
    }

    public static Account open(
            String id,
            AccountOwnerType ownerType,
            String ownerId,
            AccountType type,
            Currency currency,
            long openingBalanceCents) {
        return open(id, ownerType, ownerId, type, currency, openingBalanceCents, null);
    }

    public static Account open(
            String id,
            AccountOwnerType ownerType,
            String ownerId,
            AccountType type,
            Currency currency,
            long openingBalanceCents,
            Instant pointsExpiresAt) {
        if (openingBalanceCents < 0) {
            throw new IllegalArgumentException("opening balance must not be negative");
        }
        if (type != AccountType.POINTS && pointsExpiresAt != null) {
            throw new IllegalArgumentException("只有 POINTS 账户可以设置过期时间");
        }
        return new Account(
                requireId(id),
                Objects.requireNonNull(ownerType, "ownerType"),
                requireId(ownerId),
                Objects.requireNonNull(type, "type"),
                Objects.requireNonNull(currency, "currency"),
                openingBalanceCents,
                pointsExpiresAt);
    }

    /** 持久化回放（infrastructure → domain）。 */
    public static Account rehydrate(
            String id,
            AccountOwnerType ownerType,
            String ownerId,
            AccountType type,
            Currency currency,
            long balanceCents,
            Instant pointsExpiresAt) {
        if (balanceCents < 0) {
            throw new IllegalArgumentException("balance must not be negative");
        }
        if (type != AccountType.POINTS && pointsExpiresAt != null) {
            throw new IllegalArgumentException("只有 POINTS 账户可以设置过期时间");
        }
        return new Account(
                requireId(id),
                Objects.requireNonNull(ownerType, "ownerType"),
                requireId(ownerId),
                Objects.requireNonNull(type, "type"),
                Objects.requireNonNull(currency, "currency"),
                balanceCents,
                pointsExpiresAt);
    }

    public Account debit(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        if (balanceCents < amount) {
            throw new InsufficientBalanceException();
        }
        return new Account(id, ownerType, ownerId, type, currency, balanceCents - amount, pointsExpiresAt);
    }

    public Account credit(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        return new Account(id, ownerType, ownerId, type, currency, balanceCents + amount, pointsExpiresAt);
    }

    public boolean canCover(Money price) {
        return currency == price.currency() && balanceCents >= price.cents();
    }

    public boolean canCoverCents(long cents) {
        return balanceCents >= cents;
    }

    /** 积分在 at 时刻是否仍可用（未过期）。非 POINTS 恒为 true。 */
    public boolean pointsUsableAt(Instant at) {
        if (type != AccountType.POINTS) {
            return true;
        }
        if (pointsExpiresAt == null) {
            return true;
        }
        return at.isBefore(pointsExpiresAt);
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

    public Instant pointsExpiresAt() {
        return pointsExpiresAt;
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }

    public static final class InsufficientBalanceException extends RuntimeException {}
}
