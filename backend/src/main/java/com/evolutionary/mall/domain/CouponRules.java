package com.evolutionary.mall.domain;

import com.evolutionary.commerce.domain.Money;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 轻量优惠券规则（P5-3 决策：不引入外部引擎）。
 *
 * <ul>
 *   <li>同 mutexGroup 互斥
 *   <li>叠加 ≤ {@link #MAX_STACK}
 *   <li>MERCHANT 券仅商城；OPERATOR 券默认不作用于 MallSku（除非 SKU_LIST 显式包含）
 *   <li>MERCHANT 券不可作用于 PackageTemplate
 * </ul>
 */
public final class CouponRules {

    /** AC-44：叠加上限。 */
    public static final int MAX_STACK = 2;

    private CouponRules() {}

    /**
     * 校验商城 checkout 用券组合，并返回每张券的减免额（与输入顺序对应；按原价独立计算后截断超额）。
     */
    public static MallOutcome<List<Money>> validateAndComputeMallDiscounts(
            Money orderTotal,
            String merchantOrgId,
            List<String> cartSkuIds,
            List<CouponTemplate> templates) {
        Objects.requireNonNull(orderTotal, "orderTotal");
        Objects.requireNonNull(merchantOrgId, "merchantOrgId");
        Objects.requireNonNull(cartSkuIds, "cartSkuIds");
        Objects.requireNonNull(templates, "templates");

        if (templates.size() > MAX_STACK) {
            return MallOutcome.err(MallErrorCode.COUPON_STACK_LIMIT, "优惠券叠加不得超过 " + MAX_STACK);
        }

        Set<String> mutexSeen = new HashSet<>();
        for (CouponTemplate t : templates) {
            if (!mutexSeen.add(t.mutexGroup())) {
                return MallOutcome.err(MallErrorCode.COUPON_MUTEX_VIOLATION, "同互斥组不可叠加");
            }
        }

        List<Money> discounts = new ArrayList<>(templates.size());
        for (CouponTemplate t : templates) {
            MallOutcome<Void> scope = assertApplicableToMall(t, merchantOrgId, cartSkuIds);
            if (scope instanceof MallOutcome.Err<Void> err) {
                return MallOutcome.err(err.code(), err.message());
            }
            if (!t.meetsMinSpend(orderTotal)) {
                return MallOutcome.err(MallErrorCode.COUPON_MIN_SPEND_NOT_MET, "未达最低消费");
            }
            discounts.add(t.computeDiscount(orderTotal));
        }

        long sum = discounts.stream().mapToLong(Money::cents).sum();
        if (sum > orderTotal.cents() && !discounts.isEmpty()) {
            long over = sum - orderTotal.cents();
            int last = discounts.size() - 1;
            Money lastD = discounts.get(last);
            discounts.set(
                    last, new Money(Math.max(0, lastD.cents() - over), lastD.currency()));
        }
        return MallOutcome.ok(List.copyOf(discounts));
    }

    public static Money sumDiscounts(List<Money> parts, Money orderTotal) {
        long sum = parts.stream().mapToLong(Money::cents).sum();
        return new Money(Math.min(sum, orderTotal.cents()), orderTotal.currency());
    }

    /** AC-47 / §5：运营商券默认不作用于商城。 */
    public static MallOutcome<Void> assertApplicableToMall(
            CouponTemplate template, String merchantOrgId, List<String> cartSkuIds) {
        Objects.requireNonNull(template, "template");
        if (template.issuerType() == IssuerType.MERCHANT) {
            if (!template.issuerOrgId().equals(merchantOrgId)) {
                return MallOutcome.err(MallErrorCode.COUPON_SCOPE_MISMATCH, "商家券仅限本店");
            }
            return assertSkuScope(template, cartSkuIds);
        }
        // OPERATOR：默认拒绝商城，除非 SKU_LIST 显式列出购物车 SKU
        if (template.scope() == CouponScope.SKU_LIST
                && !template.scopeIds().isEmpty()
                && cartSkuIds.stream().allMatch(template.scopeIds()::contains)) {
            return MallOutcome.ok(null);
        }
        return MallOutcome.err(
                MallErrorCode.COUPON_SCOPE_MISMATCH, "运营商券默认不作用于商城订单");
    }

    /** AC-46：商家券不可作用于换电 PackageTemplate。 */
    public static MallOutcome<Void> assertApplicableToPackageTemplate(CouponTemplate template) {
        Objects.requireNonNull(template, "template");
        if (template.issuerType() == IssuerType.MERCHANT) {
            return MallOutcome.err(
                    MallErrorCode.COUPON_SCOPE_MISMATCH, "商家券不可用于套餐模板购买");
        }
        return MallOutcome.ok(null);
    }

    private static MallOutcome<Void> assertSkuScope(
            CouponTemplate template, List<String> cartSkuIds) {
        return switch (template.scope()) {
            case ALL_SKU -> MallOutcome.ok(null);
            case SKU_LIST -> {
                if (cartSkuIds.stream().allMatch(template.scopeIds()::contains)) {
                    yield MallOutcome.ok(null);
                }
                yield MallOutcome.err(MallErrorCode.COUPON_SCOPE_MISMATCH, "SKU 不在券适用列表");
            }
            case CATEGORY -> MallOutcome.ok(null); // 轻量：商城阶段不细分类目
        };
    }
}
