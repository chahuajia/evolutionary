/**
 * 钱包展示模型 — 视图边界。
 *
 * 对齐后端 `Account`：
 * - `canCoverCents`：balance >= amount
 * - `debit`：不足抛 `InsufficientBalanceException`
 */

import { formatCentsAsYuan } from "@/shared/money/format-cents";

export { formatCentsAsYuan };

export type WalletView = {
  readonly userId: string;
  readonly balanceCents: number;
  readonly pointsCents: number;
  readonly currency: string;
  readonly balanceYuan: string;
  readonly pointsYuan: string;
  /** 余额是否可用于任意正额扣款（对齐 canCoverCents(1) 的下限）。 */
  readonly hasSpendableBalance: boolean;
  /** 积分是否有可用额度。 */
  readonly hasSpendablePoints: boolean;
};

/**
 * 展示不变量：余额能否覆盖指定金额。
 * 对齐 `Account.canCoverCents` / `debit` 前置。
 */
export function canCoverCents(
  balanceCents: number,
  amountCents: number,
): boolean {
  if (amountCents < 0) return false;
  return balanceCents >= amountCents;
}

/** 有正余额才谈得上「可花」。 */
export function hasSpendableBalance(balanceCents: number): boolean {
  return balanceCents > 0;
}

export function toWalletView(dto: {
  userId: string;
  balanceCents: number;
  pointsCents: number;
  currency: string;
}): WalletView {
  return {
    userId: dto.userId,
    balanceCents: dto.balanceCents,
    pointsCents: dto.pointsCents,
    currency: dto.currency,
    balanceYuan: formatCentsAsYuan(dto.balanceCents),
    pointsYuan: formatCentsAsYuan(dto.pointsCents),
    hasSpendableBalance: hasSpendableBalance(dto.balanceCents),
    hasSpendablePoints: hasSpendableBalance(dto.pointsCents),
  };
}
