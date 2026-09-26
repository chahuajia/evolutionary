/**
 * 用例：商城余额购（编排 gateway → MallOrderView）。
 */

import {
  toMallOrderView,
  type MallOrderView,
} from "@/domains/mall/domain/mall-order-view";
import {
  DEFAULT_MALL_MERCHANT,
  DEFAULT_MALL_SKU,
  DEFAULT_MALL_USER,
  postPurchaseMallOrder,
} from "@/domains/mall/infrastructure/mall-gateway";

export type PurchaseMallOrderInput = {
  readonly userId: string;
  readonly merchantOrgId?: string;
  readonly skuId?: string;
  readonly qty?: number;
};

export async function runPurchaseMallOrder(
  req: PurchaseMallOrderInput,
): Promise<MallOrderView> {
  const r = await postPurchaseMallOrder({
    userId: req.userId.trim() || DEFAULT_MALL_USER,
    merchantOrgId: req.merchantOrgId?.trim() || DEFAULT_MALL_MERCHANT,
    skuId: req.skuId?.trim() || DEFAULT_MALL_SKU,
    qty: req.qty ?? 1,
  });
  return toMallOrderView(r);
}

export {
  DEFAULT_MALL_MERCHANT,
  DEFAULT_MALL_SKU,
  DEFAULT_MALL_USER,
};
