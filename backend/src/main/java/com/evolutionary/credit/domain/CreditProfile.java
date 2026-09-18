package com.evolutionary.credit.domain;

import com.evolutionary.commerce.domain.Money;
import java.util.Objects;

/** 用户信用档案（P6-1）。 */
public final class CreditProfile {

    /**
     * 信用档案状态。
     */
    public enum Status {
        GOOD,
        OVERDUE,
        FROZEN
    }


    private final String userId;
    private final Money creditLimit;
    private final Money usedCredit;
    private final CreditProfile.Status status;
    private final ScoreTier scoreTier;
    private final int policyVersion;

    private CreditProfile(
            String userId,
            Money creditLimit,
            Money usedCredit,
            CreditProfile.Status status,
            ScoreTier scoreTier,
            int policyVersion) {
        this.userId = userId;
        this.creditLimit = creditLimit;
        this.usedCredit = usedCredit;
        this.status = status;
        this.scoreTier = scoreTier;
        this.policyVersion = policyVersion;
    }

    public static CreditProfile open(
            String userId, Money creditLimit, ScoreTier tier, int policyVersion) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        Objects.requireNonNull(creditLimit, "creditLimit");
        return new CreditProfile(
                userId,
                creditLimit,
                Money.cny(0),
                CreditProfile.Status.GOOD,
                Objects.requireNonNull(tier, "tier"),
                policyVersion);
    }

    /** 持久化回放（infrastructure → domain）。 */
    public static CreditProfile rehydrate(
            String userId,
            Money creditLimit,
            Money usedCredit,
            CreditProfile.Status status,
            ScoreTier scoreTier,
            int policyVersion) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        return new CreditProfile(
                userId,
                Objects.requireNonNull(creditLimit, "creditLimit"),
                Objects.requireNonNull(usedCredit, "usedCredit"),
                Objects.requireNonNull(status, "status"),
                Objects.requireNonNull(scoreTier, "scoreTier"),
                policyVersion);
    }

    public Money availableCredit() {
        return Money.cny(Math.max(0, creditLimit.cents() - usedCredit.cents()));
    }

    public boolean canCharge(Money amount) {
        Objects.requireNonNull(amount, "amount");
        if (status != CreditProfile.Status.GOOD) {
            return false;
        }
        return usedCredit.cents() + amount.cents() <= creditLimit.cents();
    }

    public CreditOutcome<CreditProfile> charge(Money amount) {
        if (status == CreditProfile.Status.FROZEN) {
            return CreditOutcome.err(CreditErrorCode.CREDIT_PROFILE_FROZEN, "信用已冻结");
        }
        if (status == CreditProfile.Status.OVERDUE) {
            return CreditOutcome.err(CreditErrorCode.CREDIT_OVERDUE_BLOCKED, "信用逾期不可新购");
        }
        if (!canCharge(amount)) {
            return CreditOutcome.err(CreditErrorCode.CREDIT_LIMIT_EXCEEDED, "可用额度不足");
        }
        return CreditOutcome.ok(
                new CreditProfile(
                        userId,
                        creditLimit,
                        Money.cny(usedCredit.cents() + amount.cents()),
                        status,
                        scoreTier,
                        policyVersion));
    }

    public CreditProfile repay(Money amount) {
        long next = Math.max(0, usedCredit.cents() - amount.cents());
        CreditProfile.Status restored =
                (status == CreditProfile.Status.OVERDUE || status == CreditProfile.Status.FROZEN)
                        ? CreditProfile.Status.GOOD
                        : status;
        return new CreditProfile(
                userId, creditLimit, Money.cny(next), restored, scoreTier, policyVersion);
    }

    public CreditProfile markOverdue() {
        return new CreditProfile(
                userId, creditLimit, usedCredit, CreditProfile.Status.OVERDUE, scoreTier, policyVersion);
    }

    public CreditProfile markFrozen() {
        return new CreditProfile(
                userId, creditLimit, usedCredit, CreditProfile.Status.FROZEN, scoreTier, policyVersion);
    }

    /** 政策降额：仅改 limit，不清零 usedCredit。 */
    public CreditProfile withLimit(Money newLimit, int newPolicyVersion) {
        return new CreditProfile(
                userId,
                Objects.requireNonNull(newLimit, "newLimit"),
                usedCredit,
                status,
                scoreTier,
                newPolicyVersion);
    }

    public String userId() {
        return userId;
    }

    public Money creditLimit() {
        return creditLimit;
    }

    public Money usedCredit() {
        return usedCredit;
    }

    public CreditProfile.Status status() {
        return status;
    }

    public ScoreTier scoreTier() {
        return scoreTier;
    }

    public int policyVersion() {
        return policyVersion;
    }
}
