/**
 * 信用购结果展示模型 — 视图边界。
 */

import { formatCentsAsYuan } from "@/shared/money/format-cents";

export type CreditPurchaseView = {
  readonly orderId: string;
  readonly entitlementId: string;
  readonly productId: string | null;
  readonly userId: string | null;
  readonly paidAmountCents: number | null;
  readonly debtId: string | null;
  /** 面板可直接展示的摘要行。 */
  readonly summary: string;
};

export function toCreditPurchaseView(dto: {
  orderId: string;
  entitlementId: string;
  productId?: string | null;
  userId?: string | null;
  paidAmountCents?: number | null;
  debtId?: string | null;
}): CreditPurchaseView {
  const productId = dto.productId ?? null;
  const userId = dto.userId ?? null;
  const paidAmountCents =
    dto.paidAmountCents != null && Number.isFinite(dto.paidAmountCents)
      ? dto.paidAmountCents
      : null;
  const debtId = dto.debtId ?? null;
  const parts = [`订单 ${dto.orderId}`, `权益 ${dto.entitlementId}`];
  if (productId) parts.push(`商品 ${productId}`);
  if (paidAmountCents != null) {
    parts.push(`金额 ¥${formatCentsAsYuan(paidAmountCents)}`);
  }
  if (debtId) parts.push(`债务 ${debtId}`);
  return {
    orderId: dto.orderId,
    entitlementId: dto.entitlementId,
    productId,
    userId,
    paidAmountCents,
    debtId,
    summary: parts.join(" · "),
  };
}
