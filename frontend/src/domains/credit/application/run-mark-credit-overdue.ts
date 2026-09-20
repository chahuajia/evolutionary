/**
 * 用例：标逾期（编排 gateway → CreditProfileView）。
 */

import {
  toCreditProfileView,
  type CreditProfileView,
} from "@/domains/credit/domain/credit-profile-view";
import {
  DEFAULT_CREDIT_USER,
  postMarkCreditOverdue,
} from "@/domains/credit/infrastructure/credit-gateway";

export async function runMarkCreditOverdue(input: {
  userId?: string;
  statementId: string;
}): Promise<CreditProfileView> {
  const profile = await postMarkCreditOverdue({
    userId: input.userId?.trim() || DEFAULT_CREDIT_USER,
    statementId: input.statementId,
  });
  return toCreditProfileView(profile);
}

export { DEFAULT_CREDIT_USER };
