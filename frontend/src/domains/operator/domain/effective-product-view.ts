/**
 * 套餐有效价展示模型 — 视图边界。
 *
 * 对齐 GET effective-product（AC-26）：模板价经覆盖后的有效展示。
 */

import { formatCentsAsYuan } from "@/shared/money/format-cents";

export type EffectiveProductView = {
  readonly templateId: string;
  readonly templateVersion: number;
  readonly overrideId: string | null;
  /** 有覆盖时的说明；无覆盖为「无覆盖」。 */
  readonly overrideLabel: string;
  readonly priceCents: number;
  readonly priceYuan: string;
  readonly displayName: string;
  readonly durationDays: number;
};

export function toEffectiveProductView(dto: {
  templateId: string;
  templateVersion: number;
  overrideId: string | null;
  priceCents: number;
  displayName: string;
  durationDays: number;
}): EffectiveProductView {
  const overrideId = dto.overrideId;
  return {
    templateId: dto.templateId,
    templateVersion: dto.templateVersion,
    overrideId,
    overrideLabel: overrideId ? `覆盖 ${overrideId}` : "无覆盖",
    priceCents: dto.priceCents,
    priceYuan: formatCentsAsYuan(dto.priceCents),
    displayName: dto.displayName,
    durationDays: dto.durationDays,
  };
}
