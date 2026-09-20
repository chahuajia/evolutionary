/**
 * 用例：信用还款（编排 gateway）。
 */

import {
  DEFAULT_CREDIT_USER,
  postCreditRepay,
  type CreditRepayRequest,
} from "@/domains/credit/infrastructure/credit-gateway";

export async function runCreditRepay(
  req: CreditRepayRequest,
): Promise<void> {
  await postCreditRepay({
    userId: req.userId.trim() || DEFAULT_CREDIT_USER,
    statementId: req.statementId,
    amountCents: req.amountCents,
  });
}

export { DEFAULT_CREDIT_USER };
