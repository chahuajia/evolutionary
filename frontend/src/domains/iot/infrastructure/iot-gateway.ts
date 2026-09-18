/**
 * IoT BC 防腐层 — HTTP gateway（DTO → 前端读模型）
 * RSC 直连 Spring；浏览器经 Next `/api` rewrite。
 *
 * 对接 BE DetectCommLostView：
 * `{ batteryId, stale, raised, alertType, ticketId }`
 *
 * 对接 BE ShadowView（telemetry POST）：
 * `{ batteryId, soc, voltageMilli, stale, lastSeenAt, … }`
 *
 * 对接 BE TriageOutdatedSoc（假设 View / Report 序列化）：
 * `{ nextStep, orderedChecks, shadow: { batteryId, soc, voltageMilli, stale, lastSeenAt, … } }`
 * 路径：POST /iot/batteries/{id}/triage-outdated-soc（与 detect-comm-lost 同风格）
 */

import { apiBase } from "@/shared/http/api-base";
import { fetchJson } from "@/shared/http/fetch-json";

export const DEFAULT_IOT_BATTERY = "BAT-IOT-1";

export const DEFAULT_TELEMETRY_VENDOR = "vendorA";
export const DEFAULT_TELEMETRY_SOC = 75;
export const DEFAULT_TELEMETRY_VOLTAGE_MILLI = 4150;

const TIMEOUT_MS = 8000;

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
  const base = apiBase();
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

/** POST /iot/batteries/{id}/telemetry 请求体 */
export type TelemetryPayload = {
  vendorId: string;
  soc: number;
  voltageMilli: number;
};

/** POST /iot/batteries/{id}/telemetry 读模型（影子摘要） */
export type TelemetryResult = {
  batteryId: string;
  soc: number;
  voltageMilli: number;
  stale: boolean;
  lastSeenAt: string | null;
};

/**
 * POST /iot/batteries/{batteryId}/telemetry
 * 错误经 fetchJson 已拼 suggestion。
 */
export async function postTelemetry(
  batteryId: string = DEFAULT_IOT_BATTERY,
  payload: TelemetryPayload = {
    vendorId: DEFAULT_TELEMETRY_VENDOR,
    soc: DEFAULT_TELEMETRY_SOC,
    voltageMilli: DEFAULT_TELEMETRY_VOLTAGE_MILLI,
  },
): Promise<TelemetryResult> {
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/iot/batteries/${encodeURIComponent(batteryId)}/telemetry`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseTelemetryResult(raw, batteryId);
}

function parseTelemetryResult(
  raw: Record<string, unknown>,
  fallbackBatteryId: string,
): TelemetryResult {
  const batteryId = String(raw.batteryId ?? fallbackBatteryId);
  const soc = Number(raw.soc ?? 0);
  const voltageMilli = Number(raw.voltageMilli ?? 0);
  const stale = Boolean(raw.stale);
  const lastSeenAt =
    raw.lastSeenAt == null || String(raw.lastSeenAt).length === 0
      ? null
      : String(raw.lastSeenAt);

  return { batteryId, soc, voltageMilli, stale, lastSeenAt };
}

/** TriageOutdatedSoc.NextStep（BE 枚举名） */
export type TriageNextStep = "SHADOW_STALE" | "CHECK_ADAPTER" | string;

/** 诊断报告中的影子摘要（对齐 ShadowView 常用字段） */
export type TriageShadowSummary = {
  batteryId: string;
  soc: number;
  voltageMilli: number;
  stale: boolean;
  lastSeenAt: string | null;
};

/**
 * POST /iot/batteries/{id}/triage-outdated-soc 读模型
 * 含 nextStep / orderedChecks / shadow（AC-61）
 */
export type TriageOutdatedSocResult = {
  batteryId: string;
  nextStep: TriageNextStep;
  orderedChecks: string[];
  shadow: TriageShadowSummary;
};

/**
 * POST /iot/batteries/{batteryId}/triage-outdated-soc
 * 错误经 fetchJson 已拼 suggestion。
 */
export async function postTriageOutdatedSoc(
  batteryId: string = DEFAULT_IOT_BATTERY,
): Promise<TriageOutdatedSocResult> {
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/iot/batteries/${encodeURIComponent(batteryId)}/triage-outdated-soc`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseTriageOutdatedSoc(raw, batteryId);
}

function parseTriageOutdatedSoc(
  raw: Record<string, unknown>,
  fallbackBatteryId: string,
): TriageOutdatedSocResult {
  const shadowRaw =
    raw.shadow != null && typeof raw.shadow === "object"
      ? (raw.shadow as Record<string, unknown>)
      : raw;
  const shadow = parseTelemetryResult(shadowRaw, fallbackBatteryId);
  const nextStep = String(raw.nextStep ?? "");
  const orderedChecks = Array.isArray(raw.orderedChecks)
    ? raw.orderedChecks.map((c) => String(c))
    : [];
  const batteryId = String(
    raw.batteryId ?? shadow.batteryId ?? fallbackBatteryId,
  );

  return {
    batteryId,
    nextStep,
    orderedChecks,
    shadow,
  };
}

/** GET /iot/batteries/{id}/tickets 读模型 */
export type MaintenanceTicketItem = {
  ticketId: string;
  batteryId: string;
  alertType: string;
  status: string;
  createdAt: string | null;
};

/**
 * GET /iot/batteries/{batteryId}/tickets
 */
export async function fetchMaintenanceTickets(
  batteryId: string = DEFAULT_IOT_BATTERY,
): Promise<MaintenanceTicketItem[]> {
  const base = apiBase();
  const raw = await fetchJson<unknown>(
    `${base}/iot/batteries/${encodeURIComponent(batteryId)}/tickets`,
    {
      method: "GET",
      timeoutMs: TIMEOUT_MS,
    },
  );
  if (!Array.isArray(raw)) {
    throw new Error("工单列表响应不是数组");
  }
  return raw.map((item) => {
    const r = item as Record<string, unknown>;
    return {
      ticketId: String(r.ticketId ?? ""),
      batteryId: String(r.batteryId ?? batteryId),
      alertType: String(r.alertType ?? ""),
      status: String(r.status ?? ""),
      createdAt:
        r.createdAt == null || String(r.createdAt).length === 0
          ? null
          : String(r.createdAt),
    };
  });
}
