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
  /** 冻结/逾期不可信用购；仅 good 可提交 */
  readonly purchaseAllowed: boolean;
};

/** 分转元数值 */
export function centsToYuan(cents: number): number {
  return cents / 100;
}

export { formatYuan };

/**
 * 展示不变量：可用额度 = 总额度 − 已用。
 * 已用超过额度时为负数，表示超限，不在视图层偷偷截成 0。
 */
export function availableCreditCents(
  limitCents: number,
  usedCents: number,
): number {
  return limitCents - usedCents;
}

/**
 * 展示不变量：仅正常档案可信用购。
 * 对齐后端 CreditProfile.charge：frozen / overdue 直接拒。
 */
export function canPurchaseOnCredit(status: CreditStatus): boolean {
  return status === "good";
}

/** gateway DTO/读模型 → 展示模型 */
export function toCreditProfileView(profile: CreditProfile): CreditProfileView {
  const available = availableCreditCents(
    profile.creditLimit,
    profile.usedCredit,
  );
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
    purchaseAllowed: canPurchaseOnCredit(profile.status),
  };
}
