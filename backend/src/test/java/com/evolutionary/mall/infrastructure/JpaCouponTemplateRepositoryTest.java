package com.evolutionary.mall.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.mall.application.CouponTemplateRepository;
import com.evolutionary.mall.domain.CouponKind;
import com.evolutionary.mall.domain.CouponScope;
import com.evolutionary.mall.domain.CouponTemplate;
import com.evolutionary.mall.domain.IssuerType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 切片41a：CouponTemplate JPA 落库 coupon_templates。 */
@SpringBootTest
class JpaCouponTemplateRepositoryTest {

    private static final Instant VALID_FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant VALID_UNTIL = Instant.parse("2027-01-01T00:00:00Z");

    @Autowired
    private CouponTemplateRepository templates;

    @Autowired
    private CouponTemplateJpaRepository jpa;

    @BeforeEach
    void clean() {
        jpa.deleteAllInBatch();
    }

    @Test
    @DisplayName("create → save → findById")
    void saveAndFindById() {
        CouponTemplate created =
                CouponTemplate.create(
                        "T-JPA-1",
                        "M1",
                        IssuerType.MERCHANT,
                        CouponKind.FIXED_OFF,
                        500,
                        Money.cny(3_000),
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG1",
                        "CAMP-OK",
                        VALID_FROM,
                        VALID_UNTIL);
        templates.save(created);

        CouponTemplate found = templates.findById("T-JPA-1").orElseThrow();
        assertEquals("T-JPA-1", found.id());
        assertEquals("M1", found.issuerOrgId());
        assertEquals(IssuerType.MERCHANT, found.issuerType());
        assertEquals(CouponKind.FIXED_OFF, found.kind());
        assertEquals(500, found.value());
        assertEquals(3_000, found.minSpend().orElseThrow().cents());
        assertEquals(CouponScope.ALL_SKU, found.scope());
        assertEquals(List.of(), found.scopeIds());
        assertEquals("MG1", found.mutexGroup());
        assertEquals("CAMP-OK", found.campaignId());
        assertEquals(VALID_FROM, found.validFrom());
        assertEquals(VALID_UNTIL, found.validUntil());
        assertTrue(jpa.findById("T-JPA-1").isPresent());
    }

    @Test
    @DisplayName("scopeIds CSV 往返")
    void scopeIdsCsvRoundTrip() {
        CouponTemplate created =
                CouponTemplate.create(
                        "T-JPA-2",
                        "M1",
                        IssuerType.MERCHANT,
                        CouponKind.PERCENT_OFF,
                        20,
                        null,
                        CouponScope.SKU_LIST,
                        List.of("S1", "S2"),
                        "MG2",
                        null,
                        VALID_FROM,
                        VALID_UNTIL);
        templates.save(created);

        CouponTemplate found = templates.findById("T-JPA-2").orElseThrow();
        assertEquals(CouponKind.PERCENT_OFF, found.kind());
        assertTrue(found.minSpend().isEmpty());
        assertEquals(List.of("S1", "S2"), found.scopeIds());
        assertEquals("S1,S2", jpa.findById("T-JPA-2").orElseThrow().getScopeIdsCsv());
    }
}
