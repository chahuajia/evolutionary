package com.evolutionary.mall.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.mall.application.MerchantProfileRepository;
import com.evolutionary.mall.domain.MerchantProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片47a：MerchantProfile JPA 落库。 */
@SpringBootTest
class JpaMerchantProfileRepositoryTest {

    @Autowired
    private MerchantProfileRepository merchants;

    @Autowired
    private MerchantProfileJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("activate → save → findByOrgId 命中")
    void activateSaveAndFind() {
        MerchantProfile profile = MerchantProfile.activate("ORG-M-JPA", "黑鸟旗舰店");
        merchants.save(profile);

        MerchantProfile found = merchants.getByOrgId("ORG-M-JPA");
        assertEquals("ORG-M-JPA", found.orgId());
        assertEquals("黑鸟旗舰店", found.shopName());
        assertEquals(MerchantProfile.Status.ACTIVE, found.status());
        assertTrue(found.isActive());
        assertTrue(jpa.findByOrgId("ORG-M-JPA").isPresent());
    }

    @Test
    @DisplayName("rehydrate SUSPENDED → save → findByOrgId 回放状态")
    void rehydrateSuspendedThenSaveAndFind() {
        MerchantProfile suspended =
                MerchantProfile.rehydrate(
                        "ORG-SUSP", "暂停店", MerchantProfile.Status.SUSPENDED);
        merchants.save(suspended);

        MerchantProfile found = merchants.findByOrgId("ORG-SUSP").orElseThrow();
        assertEquals("暂停店", found.shopName());
        assertEquals(MerchantProfile.Status.SUSPENDED, found.status());
        assertFalse(found.isActive());
        assertEquals(MerchantProfile.Status.SUSPENDED, jpa.findByOrgId("ORG-SUSP").orElseThrow().getStatus());
    }
}
