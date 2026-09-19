/**
 * 电池展示模型 — 视图边界。
 *
 * 对齐后端 `Battery` 状态机：
 * AVAILABLE → IN_USE → CHARGING → AVAILABLE；任意非终态 → RETIRED。
 */

export type BatteryStatus =
  | "AVAILABLE"
  | "IN_USE"
  | "CHARGING"
  | "RETIRED";

const BATTERY_STATUSES = [
  "AVAILABLE",
  "IN_USE",
  "CHARGING",
  "RETIRED",
] as const;

export function parseBatteryStatus(raw: unknown): BatteryStatus {
  if (
    typeof raw === "string" &&
    (BATTERY_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as BatteryStatus;
  }
  throw new Error(
    `未知电池状态：${String(raw)}（契约：${BATTERY_STATUSES.join(" | ")}）`,
  );
}

export type BatteryView = {
  readonly id: string;
  readonly status: BatteryStatus;
  readonly statusLabel: string;
  readonly swapOutAllowed: boolean;
  readonly returnForChargingAllowed: boolean;
  readonly finishChargingAllowed: boolean;
  readonly retireAllowed: boolean;
  readonly blockMessage: string | null;
};

export const BATTERY_STATUS_LABEL: Record<BatteryStatus, string> = {
  AVAILABLE: "可换出",
  IN_USE: "使用中",
  CHARGING: "充电中",
  RETIRED: "已退役",
};

/** AVAILABLE → IN_USE */
export function canSwapOutBattery(status: BatteryStatus): boolean {
  return status === "AVAILABLE";
}

/** IN_USE → CHARGING */
export function canReturnBatteryForCharging(status: BatteryStatus): boolean {
  return status === "IN_USE";
}

/** CHARGING → AVAILABLE */
export function canFinishBatteryCharging(status: BatteryStatus): boolean {
  return status === "CHARGING";
}

/** 任意非 RETIRED → RETIRED */
export function canRetireBattery(status: BatteryStatus): boolean {
  return status !== "RETIRED";
}

export function batteryBlockMessage(status: BatteryStatus): string | null {
  if (status === "AVAILABLE") return null;
  if (status === "IN_USE") return "电池使用中，不可再换出";
  if (status === "CHARGING") return "电池充电中，不可换出";
  return "电池已退役，不再参与换电";
}

export function toBatteryView(dto: {
  id: string;
  status: BatteryStatus;
}): BatteryView {
  return {
    id: dto.id,
    status: dto.status,
    statusLabel: BATTERY_STATUS_LABEL[dto.status],
    swapOutAllowed: canSwapOutBattery(dto.status),
    returnForChargingAllowed: canReturnBatteryForCharging(dto.status),
    finishChargingAllowed: canFinishBatteryCharging(dto.status),
    retireAllowed: canRetireBattery(dto.status),
    blockMessage: batteryBlockMessage(dto.status),
  };
}
