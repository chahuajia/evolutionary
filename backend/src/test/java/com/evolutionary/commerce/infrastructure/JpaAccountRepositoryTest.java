package com.evolutionary.commerce.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片35b：Account JPA 落库。 */
@SpringBootTest
class JpaAccountRepositoryTest {

    private static final Instant EXPIRES = Instant.parse("2026-12-31T00:00:00Z");

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private AccountJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("save BALANCE → findUserBalance / get 命中")
    void saveAndFindUserBalance() {
        accounts.save(
                Account.open(
                        "ACC-T-BAL",
                        AccountOwnerType.USER,
                        "U-T",
                        AccountType.BALANCE,
                        Currency.CNY,
                        5_000));

        Account found = accounts.findUserBalance("U-T", Currency.CNY);
        assertEquals("ACC-T-BAL", found.id());
        assertEquals(5_000, found.balanceCents());
        assertEquals(AccountType.BALANCE, found.type());

        Account byId = accounts.get("ACC-T-BAL");
        assertEquals(5_000, byId.balanceCents());
        assertTrue(jpa.findById("ACC-T-BAL").isPresent());
    }

    @Test
    @DisplayName("POINTS 过期与 ORG SETTLEMENT 可回放")
    void pointsAndSettlementPersist() {
        accounts.save(
                Account.open(
                        "ACC-T-PTS",
                        AccountOwnerType.USER,
                        "U-T",
                        AccountType.POINTS,
                        Currency.CNY,
                        100,
                        EXPIRES));
        accounts.save(
                Account.open(
                        "ACC-T-SETTLE",
                        AccountOwnerType.ORG,
                        "ORG-T",
                        AccountType.SETTLEMENT,
                        Currency.CNY,
                        0));

        Account points = accounts.findUserPoints("U-T", Currency.CNY);
        assertEquals(EXPIRES, points.pointsExpiresAt());
        assertEquals(100, points.balanceCents());

        Account settle = accounts.findOrgSettlement("ORG-T", Currency.CNY);
        assertEquals("ACC-T-SETTLE", settle.id());
        assertEquals(AccountType.SETTLEMENT, settle.type());
    }
}
