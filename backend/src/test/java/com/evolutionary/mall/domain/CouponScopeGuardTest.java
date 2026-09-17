package com.evolutionary.mall.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.evolutionary.commerce.domain.Money;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AC-46：商家券不可作用于换电 PackageTemplate。 */
class CouponScopeGuardTest {

    private static final Instant VALID_FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant VALID_UNTIL = Instant.parse("2027-01-01T00:00:00Z");

    @Test
    @DisplayName("AC-46：MERCHANT 券用于 PackageTemplate → COUPON_SCOPE_MISMATCH")
    void merchantCouponRejectedOnPackageTemplate() {
        CouponTemplate merchant =
                CouponTemplate.create(
                        "T-C1",
                        "M1",
                        IssuerType.MERCHANT,
                        CouponKind.FIXED_OFF,
                        500,
                        Money.cny(3_000),
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG1",
                        "CAMP-1",
                        VALID_FROM,
                        VALID_UNTIL);

        MallOutcome<Void> outcome = CouponRules.assertApplicableToPackageTemplate(merchant);

        assertInstanceOf(MallOutcome.Err.class, outcome);
        assertEquals(
                MallErrorCode.COUPON_SCOPE_MISMATCH,
                ((MallOutcome.Err<Void>) outcome).code());
    }

    @Test
    @DisplayName("OPERATOR 券可用于 PackageTemplate（scope 守卫放行）")
    void operatorCouponAllowedOnPackageTemplate() {
        CouponTemplate operator =
                CouponTemplate.create(
                        "T-OP",
                        "OP1",
                        IssuerType.OPERATOR,
                        CouponKind.FIXED_OFF,
                        500,
                        null,
                        CouponScope.ALL_SKU,
                        List.of(),
                        "MG-OP",
                        "CAMP-OP",
                        VALID_FROM,
                        VALID_UNTIL);

        MallOutcome<Void> outcome = CouponRules.assertApplicableToPackageTemplate(operator);
        assertInstanceOf(MallOutcome.Ok.class, outcome);
    }
}
