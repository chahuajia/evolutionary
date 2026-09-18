/**
 * 用例：加载信用档案展示模型（编排 gateway，不写 fetch 细节）。
 */

import {
  DEFAULT_CREDIT_USER,
  fetchCreditProfile,
} from "@/domains/credit/infrastructure/credit-gateway";
import {
  toCreditProfileView,
  type CreditProfileView,
} from "@/domains/credit/domain/credit-profile-view";

export async function loadCreditProfile(
  userId: string = DEFAULT_CREDIT_USER,
): Promise<CreditProfileView> {
  const profile = await fetchCreditProfile(userId);
  return toCreditProfileView(profile);
}

export { DEFAULT_CREDIT_USER };
