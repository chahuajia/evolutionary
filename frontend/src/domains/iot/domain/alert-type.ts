/**
 * IoT 告警类型契约 — 视图边界共用。
 *
 * 对齐后端 `AlertType`：OVERHEAT | LOW_SOC | COMM_LOST。
 */

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
