/**
 * 信用档案展示模型 — 视图边界，非后端聚合拷贝。
 */

import {
  CREDIT_STATUS_LABEL,
  formatYuan,
  type CreditProfile,
  type CreditStatus,
  type ScoreTier,
} from "@/lib/credit/types";

export type CreditProfileView = {
  readonly userId: string;
  readonly creditLimitCents: number;
  readonly usedCreditCents: number;
  readonly availableCreditCents: number;
  readonly status: CreditStatus;
  readonly statusLabel: string;
  readonly scoreTier: ScoreTier;
  readonly policyVersion: number;
  /** 分 → 元展示串 */
  readonly availableYuan: string;
  readonly usedYuan: string;
  readonly limitYuan: string;
};

/** 分转元数值 */
export function centsToYuan(cents: number): number {
  return cents / 100;
}

export { formatYuan };

/** gateway DTO/读模型 → 展示模型 */
export function toCreditProfileView(profile: CreditProfile): CreditProfileView {
  const available = profile.creditLimit - profile.usedCredit;
  return {
    userId: profile.userId,
    creditLimitCents: profile.creditLimit,
    usedCreditCents: profile.usedCredit,
    availableCreditCents: available,
    status: profile.status,
    statusLabel: CREDIT_STATUS_LABEL[profile.status],
    scoreTier: profile.scoreTier,
    policyVersion: profile.policyVersion,
    availableYuan: formatYuan(available),
    usedYuan: formatYuan(profile.usedCredit),
    limitYuan: formatYuan(profile.creditLimit),
  };
}
