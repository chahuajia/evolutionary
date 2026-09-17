/**
 * IoT BC 防腐层 — HTTP gateway（DTO → 前端读模型）
 * RSC 直连 Spring；浏览器经 Next `/api` rewrite。
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

/** 影子摘要（展示 stale / lastSeen） */
export type ShadowSummary = {
  batteryId: string;
  stale: boolean;
  lastSeenAt?: string;
  soc?: number;
  status?: string;
};

/** COMM_LOST 告警摘要 */
export type AlertSummary = {
  batteryId: string;
  alertType: string;
  severity?: string;
  raisedAt?: string;
};

/** 运维工单摘要 */
export type TicketSummary = {
  id: string;
  batteryId: string;
  alertType: string;
  status: string;
  createdAt?: string;
};

/** POST /iot/batteries/{id}/detect-comm-lost 读模型 */
export type DetectCommLostResult = {
  batteryId: string;
  stale: boolean;
  raised: boolean;
  shadow: ShadowSummary;
  alert: AlertSummary | null;
  ticket: TicketSummary | null;
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

function asRecord(v: unknown): Record<string, unknown> | null {
  return typeof v === "object" && v != null
    ? (v as Record<string, unknown>)
    : null;
}

function parseDetectCommLost(
  raw: Record<string, unknown>,
  fallbackBatteryId: string,
): DetectCommLostResult {
  const shadowRaw = asRecord(raw.shadow);
  const alertRaw = asRecord(raw.alert);
  const ticketRaw = asRecord(raw.ticket);

  const batteryId = String(
    raw.batteryId ?? shadowRaw?.batteryId ?? fallbackBatteryId,
  );

  const stale =
    typeof raw.stale === "boolean"
      ? raw.stale
      : typeof shadowRaw?.stale === "boolean"
        ? shadowRaw.stale
        : false;

  const raised =
    typeof raw.raised === "boolean"
      ? raw.raised
      : alertRaw != null && String(alertRaw.alertType ?? "") === "COMM_LOST";

  const shadow: ShadowSummary = {
    batteryId: String(shadowRaw?.batteryId ?? batteryId),
    stale:
      typeof shadowRaw?.stale === "boolean" ? shadowRaw.stale : stale,
    lastSeenAt:
      shadowRaw?.lastSeenAt == null
        ? undefined
        : String(shadowRaw.lastSeenAt),
    soc: shadowRaw?.soc == null ? undefined : Number(shadowRaw.soc),
    status:
      shadowRaw?.status == null ? undefined : String(shadowRaw.status),
  };

  const alert: AlertSummary | null = alertRaw
    ? {
        batteryId: String(alertRaw.batteryId ?? batteryId),
        alertType: String(alertRaw.alertType ?? "COMM_LOST"),
        severity:
          alertRaw.severity == null
            ? undefined
            : String(alertRaw.severity),
        raisedAt:
          alertRaw.raisedAt == null
            ? undefined
            : String(alertRaw.raisedAt),
      }
    : null;

  const ticket: TicketSummary | null = ticketRaw
    ? {
        id: String(ticketRaw.id ?? ""),
        batteryId: String(ticketRaw.batteryId ?? batteryId),
        alertType: String(ticketRaw.alertType ?? "COMM_LOST"),
        status: String(ticketRaw.status ?? "OPEN"),
        createdAt:
          ticketRaw.createdAt == null
            ? undefined
            : String(ticketRaw.createdAt),
      }
    : null;

  return { batteryId, stale, raised, shadow, alert, ticket };
}
