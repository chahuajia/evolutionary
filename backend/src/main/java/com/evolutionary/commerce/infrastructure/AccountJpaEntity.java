package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 账户持久化模型 —— 只活在 infrastructure。 */
@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountOwnerType ownerType;

    @Column(nullable = false)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private long balanceCents;

    private Instant pointsExpiresAt;

    protected AccountJpaEntity() {}

    public AccountJpaEntity(
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

    public String getId() {
        return id;
    }

    public AccountOwnerType getOwnerType() {
        return ownerType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public AccountType getType() {
        return type;
    }

    public Currency getCurrency() {
        return currency;
    }

    public long getBalanceCents() {
        return balanceCents;
    }

    public Instant getPointsExpiresAt() {
        return pointsExpiresAt;
    }
}
