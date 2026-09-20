/**
 * 默认选卡策略 — 对齐后端 `SelectEntitlement.selectDefault`（AC-14）。
 *
 * 优先 FINITE 次卡（未用尽）；无可用次卡则用 UNLIMITED。
 */

import {
  canSwapWithEntitlement,
  isExhaustedEntitlement,
  type EntitlementStatus,
} from "./entitlement-view";

export type SelectableEntitlement = {
  readonly id: string;
  readonly status: EntitlementStatus;
  /** null = UNLIMITED；非 null = FINITE 剩余次数。 */
  readonly remainingSwaps: number | null;
};

export function isFiniteEntitlement(
  remainingSwaps: number | null,
): boolean {
  return remainingSwaps != null;
}

export { isExhaustedEntitlement };

/**
 * 展示不变量：默认选卡。
 * 对齐 `SelectEntitlement.selectDefault`（已过滤 isActiveAt / !isExhausted）。
 */
export function selectDefaultEntitlement(
  candidates: readonly SelectableEntitlement[],
): SelectableEntitlement | null {
  const usable = candidates.filter(
    (e) =>
      canSwapWithEntitlement(e.status) &&
      !isExhaustedEntitlement(e.remainingSwaps),
  );
  const finite = usable.find((e) => isFiniteEntitlement(e.remainingSwaps));
  if (finite) return finite;
  return usable[0] ?? null;
}
