/**
 * 权益换电结果展示模型 — 视图边界。
 */

import {
  toUsageEventView,
  type UsageEventView,
} from "@/domains/commerce/domain/usage-event-view";
import { formatCentsAsYuan } from "@/shared/money/format-cents";

export type EntitledSwapView = {
  readonly usageEvent: UsageEventView;
  readonly batteryId: string;
  readonly cabinetId: string;
  readonly entitlementId: string;
  readonly chargedAmountCents: number | null;
  readonly chargedAmountYuan: string | null;
};

export function toEntitledSwapView(dto: {
  usageEventId: string;
  status: unknown;
  batteryId: string;
  cabinetId: string;
  entitlementId: string;
  chargedAmountCents?: number | null;
}): EntitledSwapView {
  const chargedAmountCents =
    dto.chargedAmountCents != null && Number.isFinite(dto.chargedAmountCents)
      ? dto.chargedAmountCents
      : null;
  return {
    usageEvent: toUsageEventView({
      id: dto.usageEventId,
      status: dto.status,
    }),
    batteryId: dto.batteryId,
    cabinetId: dto.cabinetId,
    entitlementId: dto.entitlementId,
    chargedAmountCents,
    chargedAmountYuan:
      chargedAmountCents != null ? formatCentsAsYuan(chargedAmountCents) : null,
  };
}
