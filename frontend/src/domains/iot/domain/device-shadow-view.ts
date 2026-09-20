/**
 * 设备影子展示模型 — 视图边界。
 *
 * 对齐后端：
 * - `AssertShadowFreshForMetered`：stale → TELEMETRY_STALE「禁止按电量计费」
 * - `DetectCommLost`：仅 stale 时抬 COMM_LOST
 * - `TriageOutdatedSoc`：stale 时 NextStep.SHADOW_STALE
 * - `DeviceShadow.ShadowStatus` / `LockState`（ShadowView 已返回）
 */

export type ShadowStatus = "IDLE" | "RENTED" | "MAINTENANCE";
export type LockState = "LOCKED" | "UNLOCKED";

const SHADOW_STATUSES = ["IDLE", "RENTED", "MAINTENANCE"] as const;
const LOCK_STATES = ["LOCKED", "UNLOCKED"] as const;

export function parseShadowStatus(raw: unknown): ShadowStatus {
  if (
    typeof raw === "string" &&
    (SHADOW_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as ShadowStatus;
  }
  throw new Error(
    `未知影子业务态：${String(raw)}（契约：${SHADOW_STATUSES.join(" | ")}）`,
  );
}

export function parseLockState(raw: unknown): LockState {
  if (
    typeof raw === "string" &&
    (LOCK_STATES as readonly string[]).includes(raw)
  ) {
    return raw as LockState;
  }
  throw new Error(
    `未知锁态：${String(raw)}（契约：${LOCK_STATES.join(" | ")}）`,
  );
}

export const SHADOW_STATUS_LABEL: Record<ShadowStatus, string> = {
  IDLE: "空闲",
  RENTED: "租用中",
  MAINTENANCE: "运维中",
};

export const LOCK_STATE_LABEL: Record<LockState, string> = {
  LOCKED: "已锁",
  UNLOCKED: "未锁",
};

/**
 * 运维中影子不宜按正常履约展示（域枚举存在；BE 暂无硬门，仅提示）。
 */
export function isShadowInMaintenance(status: ShadowStatus): boolean {
  return status === "MAINTENANCE";
}

export type DeviceShadowView = {
  readonly batteryId: string;
  readonly soc: number;
  readonly voltageMilli: number;
  readonly stale: boolean;
  readonly lastSeenAt: string | null;
  readonly status: ShadowStatus | null;
  readonly statusLabel: string | null;
  readonly lockState: LockState | null;
  readonly lockStateLabel: string | null;
  /** 影子新鲜（!stale）。 */
  readonly fresh: boolean;
  /** 仅新鲜影子可按电量计费换电。 */
  readonly meteredSwapAllowed: boolean;
  /** 仅 stale 时值得跑 COMM_LOST 检测。 */
  readonly commLostDetectUseful: boolean;
  readonly blockMessage: string | null;
  /** 不可检测时的说明（新鲜影子）。 */
  readonly commLostDetectBlockMessage: string | null;
};

/** 展示不变量：新鲜 = 非 stale。 */
export function isShadowFresh(stale: boolean): boolean {
  return !stale;
}

/**
 * 展示不变量：仅新鲜影子可计量换电。
 * 对齐 `AssertShadowFreshForMetered`。
 */
export function canMeterWithShadow(stale: boolean): boolean {
  return !stale;
}

/**
 * 展示不变量：仅 stale 时检测 COMM_LOST 有意义。
 * 对齐 `DetectCommLost`：非 stale 直接跳过抬告警。
 */
export function canDetectCommLost(stale: boolean): boolean {
  return stale;
}

export function shadowBlockMessage(stale: boolean): string | null {
  if (!stale) return null;
  return "影子过期，禁止按电量计费 —— 先补遥测或跑通信丢失诊断";
}

/** 展示不变量：仅 stale 时检测有意义；新鲜影子给动作说明。 */
export function commLostDetectBlockMessage(stale: boolean): string | null {
  if (canDetectCommLost(stale)) return null;
  return "影子仍新鲜，检测不会抬 COMM_LOST";
}

export function toDeviceShadowView(dto: {
  batteryId: string;
  soc: number;
  voltageMilli: number;
  stale: boolean;
  lastSeenAt: string | null;
  status?: string | null;
  lockState?: string | null;
}): DeviceShadowView {
  const status =
    dto.status != null && String(dto.status).length > 0
      ? parseShadowStatus(dto.status)
      : null;
  const lockState =
    dto.lockState != null && String(dto.lockState).length > 0
      ? parseLockState(dto.lockState)
      : null;
  return {
    batteryId: dto.batteryId,
    soc: dto.soc,
    voltageMilli: dto.voltageMilli,
    stale: dto.stale,
    lastSeenAt: dto.lastSeenAt,
    status,
    statusLabel: status ? SHADOW_STATUS_LABEL[status] : null,
    lockState,
    lockStateLabel: lockState ? LOCK_STATE_LABEL[lockState] : null,
    fresh: isShadowFresh(dto.stale),
    meteredSwapAllowed: canMeterWithShadow(dto.stale),
    commLostDetectUseful: canDetectCommLost(dto.stale),
    blockMessage: shadowBlockMessage(dto.stale),
    commLostDetectBlockMessage: commLostDetectBlockMessage(dto.stale),
  };
}
