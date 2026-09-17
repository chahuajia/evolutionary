/**
 * IoT BC 防腐层 — HTTP gateway（DTO → 前端读模型）
 * RSC 直连 Spring；浏览器经 Next `/api` rewrite。
 *
 * 对接 BE DetectCommLostView：
 * `{ batteryId, stale, raised, alertType, ticketId }`
 */

import { fetchJson } from "@/shared/http/fetch-json";

export const DEFAULT_IOT_BATTERY = "BAT-IOT-1";

const TIMEOUT_MS = 8000;

function resolveIotApiBase(): string {
  if (typeof window === "undefined") {
    return process.env.BACKEND_ORIGIN ?? "http://localhost:8080";
  }
  return process.env.NEXT_PUBLIC_API_BASE ?? "/api";
}

/** POST /iot/batteries/{id}/detect-comm-lost 读模型 */
export type DetectCommLostResult = {
  batteryId: string;
  stale: boolean;
  raised: boolean;
  /** 如 COMM_LOST；未触发时为 null */
  alertType: string | null;
  /** 运维工单 id；无单时为 null */
  ticketId: string | null;
};

/**
 * POST /iot/batteries/{batteryId}/detect-comm-lost
 * 错误经 fetchJson 已拼 suggestion。
 */
export async function postDetectCommLost(
  batteryId: string = DEFAULT_IOT_BATTERY,
): Promise<DetectCommLostResult> {
  const base = resolveIotApiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/iot/batteries/${encodeURIComponent(batteryId)}/detect-comm-lost`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseDetectCommLost(raw, batteryId);
}

function parseDetectCommLost(
  raw: Record<string, unknown>,
  fallbackBatteryId: string,
): DetectCommLostResult {
  const batteryId = String(raw.batteryId ?? fallbackBatteryId);
  const stale = Boolean(raw.stale);
  const raised = Boolean(raw.raised);
  const alertType =
    raw.alertType == null || String(raw.alertType).length === 0
      ? null
      : String(raw.alertType);
  const ticketId =
    raw.ticketId == null || String(raw.ticketId).length === 0
      ? null
      : String(raw.ticketId);

  return { batteryId, stale, raised, alertType, ticketId };
}
