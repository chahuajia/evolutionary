package com.evolutionary.credit.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.CreditProfileRepository;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.credit.domain.ScoreTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片38a：CreditProfile JPA 落库。 */
@SpringBootTest
class JpaCreditProfileRepositoryTest {

    @Autowired
    private CreditProfileRepository profiles;

    @Autowired
    private CreditProfileJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("open → save → findByUserId 命中")
    void openSaveAndFindByUserId() {
        profiles.save(CreditProfile.open("U-T", Money.cny(10_000), ScoreTier.A, 1));

        CreditProfile found = profiles.get("U-T");
        assertEquals("U-T", found.userId());
        assertEquals(10_000, found.creditLimit().cents());
        assertEquals(0, found.usedCredit().cents());
        assertEquals(CreditProfile.Status.GOOD, found.status());
        assertEquals(ScoreTier.A, found.scoreTier());
        assertEquals(1, found.policyVersion());
        assertTrue(jpa.findById("U-T").isPresent());
    }

    @Test
    @DisplayName("charge 后再 save 验证 usedCredit")
    void chargeThenSavePersistsUsedCredit() {
        CreditProfile opened = CreditProfile.open("U-T2", Money.cny(10_000), ScoreTier.B, 2);
        CreditOutcome<CreditProfile> charged = opened.charge(Money.cny(3_000));
        assertTrue(charged instanceof CreditOutcome.Ok<CreditProfile>);
        profiles.save(((CreditOutcome.Ok<CreditProfile>) charged).value());

        CreditProfile found = profiles.get("U-T2");
        assertEquals(3_000, found.usedCredit().cents());
        assertEquals(10_000, found.creditLimit().cents());
        assertEquals(CreditProfile.Status.GOOD, found.status());
        assertEquals(ScoreTier.B, found.scoreTier());
        assertEquals(2, found.policyVersion());
    }
}
