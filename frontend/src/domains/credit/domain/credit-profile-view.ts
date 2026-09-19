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

export type CreditPurchaseBlock = "ok" | "status" | "limit";

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
  /** 购单被拒原因；ok 才可提交 */
  readonly purchaseBlock: CreditPurchaseBlock;
  /** 已用>0 或非 good 才提供还款 */
  readonly repayAllowed: boolean;
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

/**
 * 展示不变量：可用额度须为正才有购单余地。
 * 对齐后端 canCharge：used + amount ≤ limit；无商品价时，≤0 必拒。
 */
export function hasCreditHeadroom(availableCents: number): boolean {
  return availableCents > 0;
}

/** 状态优先于额度：frozen/overdue 先报，再报额度不足。 */
export function creditPurchaseBlock(
  status: CreditStatus,
  availableCents: number,
): CreditPurchaseBlock {
  if (!canPurchaseOnCredit(status)) return "status";
  if (!hasCreditHeadroom(availableCents)) return "limit";
  return "ok";
}

/**
 * 展示不变量：还款是恢复路径。
 * 对齐后端 repay：扣 used，且 overdue/frozen 会回到 good。
 * used=0 且 good 时还款是空操作，不提供。
 */
export function canOfferCreditRepay(
  status: CreditStatus,
  usedCents: number,
): boolean {
  return status !== "good" || usedCents > 0;
}

export function creditPurchaseBlockMessage(
  block: CreditPurchaseBlock,
  statusLabel?: string,
  availableYuan?: string,
): string | null {
  if (block === "ok") return null;
  if (block === "limit") {
    return availableYuan != null
      ? `可用额度不足（¥${availableYuan}），不可信用购`
      : "可用额度不足，不可信用购";
  }
  return statusLabel
    ? `档案${statusLabel}，不可信用购`
    : "档案状态不允许信用购";
}

/** gateway DTO/读模型 → 展示模型 */
export function toCreditProfileView(profile: CreditProfile): CreditProfileView {
  const available = availableCreditCents(
    profile.creditLimit,
    profile.usedCredit,
  );
  const block = creditPurchaseBlock(profile.status, available);
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
    purchaseAllowed: block === "ok",
    purchaseBlock: block,
    repayAllowed: canOfferCreditRepay(profile.status, profile.usedCredit),
  };
}
