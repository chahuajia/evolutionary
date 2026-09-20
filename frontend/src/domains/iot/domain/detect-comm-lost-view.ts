/**
 * 通信丢失检测展示模型 — 视图边界。
 *
 * 对齐后端 `DetectCommLost` / `AlertType`：
 * - 仅 stale 时抬 COMM_LOST 有意义
 * - alertType 有则 parse；无则 null（勿岛内猜 COMM_LOST）
 */

import { canDetectCommLost } from "./device-shadow-view";

export type AlertType = "OVERHEAT" | "LOW_SOC" | "COMM_LOST";

const ALERT_TYPES = ["OVERHEAT", "LOW_SOC", "COMM_LOST"] as const;

export function parseAlertType(raw: unknown): AlertType {
  if (
    typeof raw === "string" &&
    (ALERT_TYPES as readonly string[]).includes(raw)
  ) {
    return raw as AlertType;
  }
  throw new Error(
    `未知告警类型：${String(raw)}（契约：${ALERT_TYPES.join(" | ")}）`,
  );
}

export const ALERT_TYPE_LABEL: Record<AlertType, string> = {
  OVERHEAT: "过热",
  LOW_SOC: "低电量",
  COMM_LOST: "通信丢失",
};

export type DetectCommLostView = {
  readonly batteryId: string;
  readonly stale: boolean;
  readonly staleLabel: string;
  readonly raised: boolean;
  readonly raisedLabel: string;
  readonly alertType: AlertType | null;
  readonly alertTypeLabel: string | null;
  readonly ticketId: string | null;
  /** 本次结果上的 stale 是否仍值得检测（对齐 canDetectCommLost）。 */
  readonly detectUseful: boolean;
};

export function toDetectCommLostView(dto: {
  batteryId: string;
  stale: boolean;
  raised: boolean;
  alertType: string | null;
  ticketId: string | null;
}): DetectCommLostView {
  const alertType =
    dto.alertType != null && String(dto.alertType).length > 0
      ? parseAlertType(dto.alertType)
      : null;
  return {
    batteryId: dto.batteryId,
    stale: dto.stale,
    staleLabel: dto.stale ? "过期" : "新鲜",
    raised: dto.raised,
    raisedLabel: dto.raised ? "已抬告警" : "未抬告警",
    alertType,
    alertTypeLabel: alertType ? ALERT_TYPE_LABEL[alertType] : null,
    ticketId: dto.ticketId,
    detectUseful: canDetectCommLost(dto.stale),
  };
}
