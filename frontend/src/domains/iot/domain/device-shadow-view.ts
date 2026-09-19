/**
 * 设备影子展示模型 — 视图边界。
 *
 * 对齐后端：
 * - `AssertShadowFreshForMetered`：stale → TELEMETRY_STALE「禁止按电量计费」
 * - `DetectCommLost`：仅 stale 时抬 COMM_LOST
 * - `TriageOutdatedSoc`：stale 时 NextStep.SHADOW_STALE
 */

export type DeviceShadowView = {
  readonly batteryId: string;
  readonly soc: number;
  readonly voltageMilli: number;
  readonly stale: boolean;
  readonly lastSeenAt: string | null;
  /** 影子新鲜（!stale）。 */
  readonly fresh: boolean;
  /** 仅新鲜影子可按电量计费换电。 */
  readonly meteredSwapAllowed: boolean;
  /** 仅 stale 时值得跑 COMM_LOST 检测。 */
  readonly commLostDetectUseful: boolean;
  readonly blockMessage: string | null;
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

export function toDeviceShadowView(dto: {
  batteryId: string;
  soc: number;
  voltageMilli: number;
  stale: boolean;
  lastSeenAt: string | null;
}): DeviceShadowView {
  return {
    batteryId: dto.batteryId,
    soc: dto.soc,
    voltageMilli: dto.voltageMilli,
    stale: dto.stale,
    lastSeenAt: dto.lastSeenAt,
    fresh: isShadowFresh(dto.stale),
    meteredSwapAllowed: canMeterWithShadow(dto.stale),
    commLostDetectUseful: canDetectCommLost(dto.stale),
    blockMessage: shadowBlockMessage(dto.stale),
  };
}
