/**
 * 用例：权益换电（编排 gateway → EntitledSwapView）。
 */

import {
  toEntitledSwapView,
  type EntitledSwapView,
} from "@/domains/commerce/domain/entitled-swap-view";
import { postEntitledSwap } from "@/domains/commerce/infrastructure/entitled-swap-gateway";

export type EntitledSwapInput = {
  readonly userId: string;
  readonly entitlementId?: string;
  readonly cabinetId: string;
  readonly socBefore?: number;
  readonly socAfter?: number;
};

export type { EntitledSwapView };

export async function runEntitledSwap(
  req: EntitledSwapInput,
): Promise<EntitledSwapView> {
  const r = await postEntitledSwap(req);
  return toEntitledSwapView({
    usageEventId: r.usageEventId,
    status: r.status,
    batteryId: r.batteryId,
    cabinetId: r.cabinetId,
    entitlementId: r.entitlementId,
    chargedAmountCents: r.chargedAmountCents,
  });
}
