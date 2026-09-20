/**
 * 用例：应用信用政策（编排 gateway → CreditProfileView）。
 */

import {
  toCreditProfileView,
  type CreditProfileView,
} from "@/domains/credit/domain/credit-profile-view";
import {
  DEFAULT_CREDIT_USER,
  postApplyCreditPolicy,
} from "@/domains/credit/infrastructure/credit-gateway";

export async function runApplyCreditPolicy(input: {
  userId?: string;
  policyVersion: number;
}): Promise<CreditProfileView> {
  const profile = await postApplyCreditPolicy({
    userId: input.userId?.trim() || DEFAULT_CREDIT_USER,
    policyVersion: input.policyVersion,
  });
  return toCreditProfileView(profile);
}

export { DEFAULT_CREDIT_USER };
