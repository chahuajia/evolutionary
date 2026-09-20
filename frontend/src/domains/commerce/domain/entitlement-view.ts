/**
 * 权益展示模型 — 视图边界。
 *
 * 对齐后端 `Entitlement`：
 * - 履约：仅 ACTIVE（isActiveAt 另卡窗口）∧ !isExhausted
 * - freeze：仅 ACTIVE → FROZEN
 * - unfreeze：仅 FROZEN → ACTIVE
 * - revoke：ACTIVE / EXPIRED / FROZEN（已 REVOKED 幂等）
 */

export type EntitlementStatus = "ACTIVE" | "FROZEN" | "EXPIRED" | "REVOKED";

const ENTITLEMENT_STATUSES = [
  "ACTIVE",
  "FROZEN",
  "EXPIRED",
  "REVOKED",
] as const;

export function parseEntitlementStatus(raw: unknown): EntitlementStatus {
  if (
    typeof raw === "string" &&
    (ENTITLEMENT_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as EntitlementStatus;
  }
  throw new Error(
    `未知权益状态：${String(raw)}（契约：${ENTITLEMENT_STATUSES.join(" | ")}）`,
  );
}

export type EntitlementView = {
  readonly id: string;
  readonly status: EntitlementStatus;
  readonly statusLabel: string;
  /** null = UNLIMITED；非 null = FINITE 剩余次数。 */
  readonly remainingSwaps: number | null;
  /** 计量费率（分/%SOC）；非计量权益为 null。 */
  readonly meteredRateCents: number | null;
  /**
   * ACTIVE ∧ 未用尽可发起权益换电（窗口由后端再判）。
   * 对齐 `Entitlement.isExhausted` / `PerformEntitledSwap`。
   */
  readonly swapAllowed: boolean;
  /**
   * 可计量估费换电 = swapAllowed ∧ 已挂费率。
   * 岛勿再手拼「未挂计量费率」。
   */
  readonly meteredEstimateAllowed: boolean;
  readonly freezeAllowed: boolean;
  readonly unfreezeAllowed: boolean;
  readonly revokeAllowed: boolean;
  readonly blockMessage: string | null;
  /** 可换电但缺费率时的说明；否则 null。 */
  readonly meteredEstimateBlockMessage: string | null;
};

export const ENTITLEMENT_STATUS_LABEL: Record<EntitlementStatus, string> = {
  ACTIVE: "有效",
  FROZEN: "冻结",
  EXPIRED: "已过期",
  REVOKED: "已撤销",
};

export function canSwapWithEntitlement(status: EntitlementStatus): boolean {
  return status === "ACTIVE";
}

/** 展示不变量：FINITE 且 remaining==0 为用尽。null = UNLIMITED。 */
export function isExhaustedEntitlement(
  remainingSwaps: number | null | undefined,
): boolean {
  return remainingSwaps != null && remainingSwaps === 0;
}

export function canFreezeEntitlement(status: EntitlementStatus): boolean {
  return status === "ACTIVE";
}

export function canUnfreezeEntitlement(status: EntitlementStatus): boolean {
  return status === "FROZEN";
}

/** ACTIVE / EXPIRED / FROZEN 可撤销；REVOKED 已终态。 */
export function canRevokeEntitlement(status: EntitlementStatus): boolean {
  return (
    status === "ACTIVE" || status === "EXPIRED" || status === "FROZEN"
  );
}

export function entitlementBlockMessage(
  status: EntitlementStatus,
  remainingSwaps?: number | null,
): string | null {
  if (status === "ACTIVE") {
    if (isExhaustedEntitlement(remainingSwaps ?? null)) {
      return "权益次数已用尽，不可换电";
    }
    return null;
  }
  if (status === "FROZEN") return "权益已冻结（信用逾期），还款后方可换电";
  if (status === "EXPIRED") return "权益已过期，不可换电";
  return "权益已撤销，不可换电";
}

/** 展示不变量：已挂有限正数费率才可估费。 */
export function hasMeteredRate(rateCents: number | null | undefined): boolean {
  return rateCents != null && Number.isFinite(rateCents);
}

export function meteredRateBlockMessage(
  rateCents: number | null | undefined,
): string | null {
  if (hasMeteredRate(rateCents)) return null;
  return "权益未挂计量费率，不可估费换电";
}

/**
 * 估费：ΔSOC × 费率。换后 SOC 高于换前时不可估（返回 null）。
 */
export function estimateMeteredChargeCents(
  socBefore: number,
  socAfter: number,
  rateCents: number,
): number | null {
  const delta = socBefore - socAfter;
  if (!Number.isFinite(delta) || delta < 0) return null;
  if (!Number.isFinite(rateCents)) return null;
  return delta * rateCents;
}

/** 展示不变量：换后 SOC 不得超过换前。 */
export function meteredSocEstimateBlockMessage(
  socBefore: number,
  socAfter: number,
): string | null {
  const delta = socBefore - socAfter;
  if (!Number.isFinite(delta) || delta < 0) {
    return "换后 SOC 不得超过换前 SOC";
  }
  return null;
}

export function toEntitlementView(dto: {
  id: string;
  status: EntitlementStatus;
  remainingSwaps?: number | null;
  meteredRateCents?: number | null;
}): EntitlementView {
  const remainingSwaps =
    dto.remainingSwaps === undefined ? null : dto.remainingSwaps;
  const meteredRateCents =
    dto.meteredRateCents === undefined ? null : dto.meteredRateCents;
  const statusOk = canSwapWithEntitlement(dto.status);
  const exhausted = isExhaustedEntitlement(remainingSwaps);
  const swapAllowed = statusOk && !exhausted;
  const rateOk = hasMeteredRate(meteredRateCents);
  return {
    id: dto.id,
    status: dto.status,
    statusLabel: ENTITLEMENT_STATUS_LABEL[dto.status],
    remainingSwaps,
    meteredRateCents,
    swapAllowed,
    meteredEstimateAllowed: swapAllowed && rateOk,
    freezeAllowed: canFreezeEntitlement(dto.status),
    unfreezeAllowed: canUnfreezeEntitlement(dto.status),
    revokeAllowed: canRevokeEntitlement(dto.status),
    blockMessage: entitlementBlockMessage(dto.status, remainingSwaps),
    meteredEstimateBlockMessage: swapAllowed
      ? meteredRateBlockMessage(meteredRateCents)
      : null,
  };
}
