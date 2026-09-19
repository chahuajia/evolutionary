/**
 * 带券结账展示模型 — 视图边界。
 *
 * 结账结果仍是 `MallOrder`：状态契约与 INV-16 门与 `mall-order-view` 共用，
 * 本文件只多折扣展示。
 */

import { formatCentsAsYuan } from "@/shared/money/format-cents";
import {
  forbidsEntitlement,
  mallOrderBlockMessage,
  parseMallOrderStatus,
  type MallOrderStatus,
} from "./mall-order-view";

export type MallCheckoutView = {
  readonly orderId: string;
  readonly userId: string;
  readonly merchantOrgId: string;
  readonly status: MallOrderStatus;
  readonly paidAmountCents: number;
  readonly paidAmountYuan: string;
  readonly skuId: string;
  readonly qty: number;
  readonly discountCents: number;
  readonly discountYuan: string;
  /** 是否用了券/活动折扣。 */
  readonly hasDiscount: boolean;
  /** PAID 及之后禁止开换电权益（INV-16）。 */
  readonly entitlementForbidden: boolean;
  readonly blockMessage: string | null;
};

/** 展示不变量：折扣分须非负；>0 才算有优惠。 */
export function hasCheckoutDiscount(discountCents: number): boolean {
  return discountCents > 0;
}

export function toCheckoutView(dto: {
  orderId: string;
  userId: string;
  merchantOrgId: string;
  status: string;
  paidAmountCents: number;
  skuId: string;
  qty: number;
  discountCents: number;
}): MallCheckoutView {
  const status = parseMallOrderStatus(dto.status);
  const discountCents = dto.discountCents;
  return {
    orderId: dto.orderId,
    userId: dto.userId,
    merchantOrgId: dto.merchantOrgId,
    status,
    paidAmountCents: dto.paidAmountCents,
    paidAmountYuan: formatCentsAsYuan(dto.paidAmountCents),
    skuId: dto.skuId,
    qty: dto.qty,
    discountCents,
    discountYuan: formatCentsAsYuan(discountCents),
    hasDiscount: hasCheckoutDiscount(discountCents),
    entitlementForbidden: forbidsEntitlement(status),
    blockMessage: mallOrderBlockMessage(status),
  };
}
