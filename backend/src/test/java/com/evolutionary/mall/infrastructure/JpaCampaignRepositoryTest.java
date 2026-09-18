package com.evolutionary.mall.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.application.CampaignRepository;
import com.evolutionary.mall.domain.Campaign;
import com.evolutionary.mall.domain.CampaignStatus;
import com.evolutionary.mall.domain.MallOutcome;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片40b：Campaign JPA 落库 campaigns。 */
@SpringBootTest
class JpaCampaignRepositoryTest {

    @Autowired
    private CampaignRepository campaigns;

    @Autowired
    private CampaignJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("createActive → save → findById")
    void saveAndFindById() {
        Campaign created =
                Campaign.createActive(
                        "CAMP-JPA-1", "M1", "测试活动", Money.cny(5_000), List.of("T-C1", "T-C2"));
        campaigns.save(created);

        Campaign found = campaigns.findById("CAMP-JPA-1").orElseThrow();
        assertEquals("CAMP-JPA-1", found.id());
        assertEquals("M1", found.ownerOrgId());
        assertEquals("测试活动", found.name());
        assertEquals(5_000, found.budgetTotal().cents());
        assertEquals(5_000, found.budgetRemaining().cents());
        assertEquals(CampaignStatus.ACTIVE, found.status());
        assertEquals(List.of("T-C1", "T-C2"), found.couponTemplateIds());
        assertTrue(jpa.findById("CAMP-JPA-1").isPresent());
    }

    @Test
    @DisplayName("consumeBudget 后再 save 验证 budgetRemaining")
    void consumeBudgetThenSave() {
        Campaign created =
                Campaign.createActive(
                        "CAMP-JPA-2", "M1", "扣预算", Money.cny(1_000), List.of("T-C1"));
        campaigns.save(created);

        MallOutcome<Campaign> consumed = created.consumeBudget(Money.cny(300));
        assertInstanceOf(MallOutcome.Ok.class, consumed);
        Campaign after = ((MallOutcome.Ok<Campaign>) consumed).value();
        campaigns.save(after);

        Campaign found = campaigns.findById("CAMP-JPA-2").orElseThrow();
        assertEquals(1_000, found.budgetTotal().cents());
        assertEquals(700, found.budgetRemaining().cents());
    }
}
