/**
 * 用例：信用还款（编排 gateway）。
 */

import {
  DEFAULT_CREDIT_USER,
  postCreditRepay,
} from "@/domains/credit/infrastructure/credit-gateway";

export type CreditRepayInput = {
  readonly userId: string;
  readonly statementId: string;
  readonly amountCents: number;
};

export async function runCreditRepay(req: CreditRepayInput): Promise<void> {
  await postCreditRepay({
    userId: req.userId.trim() || DEFAULT_CREDIT_USER,
    statementId: req.statementId,
    amountCents: req.amountCents,
  });
}

export { DEFAULT_CREDIT_USER };
