/**
 * 券模板展示模型 — 视图边界（面额门，非 wire 拷贝）。
 */

import { formatCentsAsYuan } from "@/shared/money/format-cents";

export type CouponTemplateView = {
  readonly id: string;
  readonly faceBudgetCents: number;
  readonly faceYuan: string;
  readonly kind: string;
  readonly kindLabel: string;
  readonly campaignId: string;
  /** 面额已知且 >0，可供活动预算门。 */
  readonly faceKnown: boolean;
};

export function couponKindLabel(kind: string): string {
  const k = kind.trim().toUpperCase();
  if (k === "FIXED" || k === "FIXED_AMOUNT") return "固定面额";
  if (k === "PERCENT" || k === "PERCENTAGE") return "折扣比例";
  if (!k) return "未标注类型";
  return kind;
}

export function toCouponTemplateView(dto: {
  id: string;
  faceBudgetCents: number;
  kind: string;
  campaignId: string;
}): CouponTemplateView {
  const faceBudgetCents = Number.isFinite(dto.faceBudgetCents)
    ? dto.faceBudgetCents
    : 0;
  return {
    id: dto.id,
    faceBudgetCents,
    faceYuan: formatCentsAsYuan(faceBudgetCents),
    kind: dto.kind,
    kindLabel: couponKindLabel(dto.kind),
    campaignId: dto.campaignId,
    faceKnown: faceBudgetCents > 0,
  };
}
