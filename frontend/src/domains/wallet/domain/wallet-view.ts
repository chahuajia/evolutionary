/**
 * 钱包展示模型 — 视图边界。
 */

export type WalletView = {
  readonly userId: string;
  readonly balanceCents: number;
  readonly pointsCents: number;
  readonly currency: string;
  readonly balanceYuan: string;
  readonly pointsYuan: string;
};

export function formatCentsAsYuan(cents: number): string {
  return (cents / 100).toFixed(2);
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
  };
}
