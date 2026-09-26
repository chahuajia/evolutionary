/**
 * 用例：加载权益展示模型（编排 gateway → EntitlementView）。
 */

import {
  parseEntitlementStatus,
  toEntitlementView,
  type EntitlementView,
} from "@/domains/commerce/domain/entitlement-view";
import { fetchEntitlement } from "@/domains/commerce/infrastructure/entitled-swap-gateway";

export async function loadEntitlement(
  entitlementId: string,
): Promise<EntitlementView> {
  const dto = await fetchEntitlement(entitlementId);
  return toEntitlementView({
    id: dto.id,
    status: parseEntitlementStatus(dto.status),
    remainingSwaps: dto.remainingSwaps,
    meteredRateCents: dto.meteredRateCents,
  });
}
