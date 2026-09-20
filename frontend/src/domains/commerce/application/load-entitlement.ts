/**
 * 用例：加载权益展示模型（编排 gateway → EntitlementView）。
 */

import {
  parseEntitlementStatus,
  toEntitlementView,
  type EntitlementView,
} from "@/domains/commerce/domain/entitlement-view";
import { fetchEntitlement } from "@/domains/commerce/infrastructure/entitled-swap-gateway";

export type EntitlementLoad = {
  readonly view: EntitlementView;
  readonly remainingSwaps: number | null;
  readonly meteredRateCents: number | null;
};

export async function loadEntitlement(
  entitlementId: string,
): Promise<EntitlementLoad> {
  const dto = await fetchEntitlement(entitlementId);
  return {
    remainingSwaps: dto.remainingSwaps,
    meteredRateCents: dto.meteredRateCents,
    view: toEntitlementView({
      id: dto.id,
      status: parseEntitlementStatus(dto.status),
    }),
  };
}
