/**
 * 用例：信用购（编排 gateway → CreditPurchaseView）。
 */

import {
  toCreditPurchaseView,
  type CreditPurchaseView,
} from "@/domains/credit/domain/credit-purchase-view";
import {
  DEFAULT_CREDIT_USER,
  postCreditPurchase,
} from "@/domains/credit/infrastructure/credit-gateway";

export type CreditPurchaseInput = {
  readonly userId: string;
  readonly productId: string;
};

export type { CreditPurchaseView };

export async function runCreditPurchase(
  req: CreditPurchaseInput,
): Promise<CreditPurchaseView> {
  const r = await postCreditPurchase({
    userId: req.userId.trim() || DEFAULT_CREDIT_USER,
    productId: req.productId,
  });
  return toCreditPurchaseView(r);
}

export { DEFAULT_CREDIT_USER };
