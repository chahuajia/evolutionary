/**
 * 用例：带券结账（编排 gateway → MallCheckoutView）。
 */

import {
  toCheckoutView,
  type MallCheckoutView,
} from "@/domains/mall/domain/mall-checkout-view";
import {
  DEFAULT_MALL_MERCHANT,
  DEFAULT_MALL_SKU,
  DEFAULT_MALL_USER,
  postCheckoutWithCoupons,
} from "@/domains/mall/infrastructure/mall-gateway";

export type CheckoutWithCouponsInput = {
  readonly userId: string;
  readonly merchantOrgId?: string;
  readonly skuId?: string;
  readonly qty?: number;
  readonly userCouponIds: readonly string[];
};

export async function runCheckoutWithCoupons(
  req: CheckoutWithCouponsInput,
): Promise<MallCheckoutView> {
  const r = await postCheckoutWithCoupons({
    userId: req.userId.trim() || DEFAULT_MALL_USER,
    merchantOrgId: req.merchantOrgId?.trim() || DEFAULT_MALL_MERCHANT,
    skuId: req.skuId?.trim() || DEFAULT_MALL_SKU,
    qty: req.qty ?? 1,
    userCouponIds: [...req.userCouponIds],
  });
  return toCheckoutView(r);
}

export {
  DEFAULT_MALL_MERCHANT,
  DEFAULT_MALL_SKU,
  DEFAULT_MALL_USER,
};
