/**
 * 用例：信用购（编排 gateway → 展示摘要模型）。
 */

import {
  DEFAULT_CREDIT_USER,
  postCreditPurchase,
} from "@/domains/credit/infrastructure/credit-gateway";
import { formatCentsAsYuan } from "@/shared/money/format-cents";

export type CreditPurchaseInput = {
  readonly userId: string;
  readonly productId: string;
};

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

export async function runCreditPurchase(
  req: CreditPurchaseInput,
): Promise<CreditPurchaseView> {
  const r = await postCreditPurchase({
    userId: req.userId.trim() || DEFAULT_CREDIT_USER,
    productId: req.productId,
  });
  const parts = [`订单 ${r.orderId}`, `权益 ${r.entitlementId}`];
  if (r.productId) parts.push(`商品 ${r.productId}`);
  if (r.paidAmountCents != null) {
    parts.push(`金额 ¥${formatCentsAsYuan(r.paidAmountCents)}`);
  }
  if (r.debtId) parts.push(`债务 ${r.debtId}`);
  return {
    orderId: r.orderId,
    entitlementId: r.entitlementId,
    productId: r.productId ?? null,
    userId: r.userId ?? null,
    paidAmountCents: r.paidAmountCents ?? null,
    debtId: r.debtId ?? null,
    summary: parts.join(" · "),
  };
}

export { DEFAULT_CREDIT_USER };
