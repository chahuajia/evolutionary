/**
 * 用例：加载用户 ACTIVE 权益目录（编排 gateway → SelectableEntitlement[]）。
 */

import {
  toSelectableEntitlement,
  type SelectableEntitlement,
} from "@/domains/commerce/domain/select-entitlement";
import { fetchActiveEntitlements } from "@/domains/commerce/infrastructure/entitled-swap-gateway";

export async function loadActiveEntitlements(
  userId: string,
): Promise<readonly SelectableEntitlement[]> {
  const rows = await fetchActiveEntitlements(userId);
  return rows.map(toSelectableEntitlement);
}
