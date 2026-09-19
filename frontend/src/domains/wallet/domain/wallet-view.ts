/**
 * 钱包展示模型 — 视图边界。
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
};

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
  };
}
