/**
 * 用例：权益换电（编排 gateway → UsageEventView + 读侧摘要）。
 */

import {
  toUsageEventView,
  type UsageEventView,
} from "@/domains/commerce/domain/usage-event-view";
import {
  postEntitledSwap,
  type EntitledSwapRequest,
} from "@/domains/commerce/infrastructure/entitled-swap-gateway";

export type EntitledSwapView = {
  readonly usageEvent: UsageEventView;
  readonly batteryId: string;
  readonly cabinetId: string;
  readonly entitlementId: string;
  readonly chargedAmountCents: number | null;
};

export async function runEntitledSwap(
  req: EntitledSwapRequest,
): Promise<EntitledSwapView> {
  const r = await postEntitledSwap(req);
  return {
    usageEvent: toUsageEventView({
      id: r.usageEventId,
      status: r.status,
    }),
    batteryId: r.batteryId,
    cabinetId: r.cabinetId,
    entitlementId: r.entitlementId,
    chargedAmountCents:
      r.chargedAmountCents != null ? r.chargedAmountCents : null,
  };
}
