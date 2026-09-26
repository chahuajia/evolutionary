/**
 * 换电站防腐层 — HTTP gateway（DTO → 前端读模型）
 * RSC 直连 Spring；浏览器经 Next `/api` rewrite。
 */

import { apiBase } from "@/shared/http/api-base";
import { fetchJson } from "@/shared/http/fetch-json";

export type StationSummary = {
  id: string;
  name: string;
  canSwapOut: boolean;
  batteryCount: number;
};

export type SwapLog = {
  id: string;
  stationId: string;
  outgoingBatteryId: string;
  incomingBatteryId: string;
  occurredAt: string;
};

const TIMEOUT_MS = 8000;

function triageListError(e: unknown): Error {
  const msg = e instanceof Error ? e.message : String(e);
  if (msg === "Failed to fetch" || msg.includes("NetworkError")) {
    return new Error(
      "站列表拉取失败（W1）：请确认 Spring 已启动 :8080，且 Next rewrite /api 生效。",
    );
  }
  if (msg.startsWith("HTTP ")) {
    return new Error(`站列表拉取失败（${msg}）`);
  }
  return e instanceof Error ? e : new Error(msg);
}

function parseStationSummary(raw: Record<string, unknown>): StationSummary {
  return {
    id: String(raw.id ?? ""),
    name: String(raw.name ?? ""),
    canSwapOut: Boolean(raw.canSwapOut),
    batteryCount: Number(raw.batteryCount ?? 0),
  };
}

/** 站概览列表；RSC / 客户端共用，cache: no-store。 */
export async function fetchStationSummaries(): Promise<
  readonly StationSummary[]
> {
  const base = apiBase();
  try {
    const raw = await fetchJson<unknown[]>(`${base}/stations`, {
      cache: "no-store",
      timeoutMs: TIMEOUT_MS,
    });
    if (!Array.isArray(raw)) {
      throw new Error("站列表响应格式无效");
    }
    return raw.map((row) =>
      parseStationSummary(row as Record<string, unknown>),
    );
  } catch (e) {
    throw triageListError(e);
  }
}

function parseSwapLog(raw: Record<string, unknown>): SwapLog {
  return {
    id: String(raw.id ?? ""),
    stationId: String(raw.stationId ?? ""),
    outgoingBatteryId: String(raw.outgoingBatteryId ?? ""),
    incomingBatteryId: String(raw.incomingBatteryId ?? ""),
    occurredAt: String(raw.occurredAt ?? ""),
  };
}

/** 站换电日志；GET /stations/{id}/swap-logs。 */
export async function fetchSwapLogs(
  stationId: string,
): Promise<readonly SwapLog[]> {
  const base = apiBase();
  const raw = await fetchJson<unknown[]>(
    `${base}/stations/${encodeURIComponent(stationId)}/swap-logs`,
    { cache: "no-store", timeoutMs: TIMEOUT_MS },
  );
  if (!Array.isArray(raw)) {
    throw new Error("换电日志响应格式无效");
  }
  return raw.map((row) => parseSwapLog(row as Record<string, unknown>));
}

export type StationBatteryDto = {
  id: string;
  status: string;
};

export type StationDetailDto = {
  id: string;
  name: string;
  canSwapOut: boolean;
  batteries: StationBatteryDto[];
};

export type StationSwapResult = {
  stationId: string;
  outgoingId: string;
  incomingId: string;
};

function triageStationError(e: unknown): Error {
  const msg = e instanceof Error ? e.message : String(e);
  if (msg === "Failed to fetch" || msg.includes("NetworkError")) {
    return new Error(
      "请求失败（W1）：请确认 Spring 已启动 :8080，且 Next rewrite /api 生效。",
    );
  }
  return e instanceof Error ? e : new Error(msg);
}

/** GET /stations/{id} — 站详情含电池列表。 */
export async function fetchStationDetail(
  stationId: string,
): Promise<StationDetailDto> {
  const base = apiBase();
  try {
    const raw = await fetchJson<Record<string, unknown>>(
      `${base}/stations/${encodeURIComponent(stationId)}`,
      { timeoutMs: TIMEOUT_MS },
    );
    const batteriesRaw = Array.isArray(raw.batteries) ? raw.batteries : [];
    return {
      id: String(raw.id ?? stationId),
      name: String(raw.name ?? ""),
      canSwapOut: Boolean(raw.canSwapOut),
      batteries: batteriesRaw.map((row) => {
        const b = row as Record<string, unknown>;
        return {
          id: String(b.id ?? ""),
          status: String(b.status ?? ""),
        };
      }),
    };
  } catch (e) {
    throw triageStationError(e);
  }
}

/** POST /stations/{id}/swaps */
export async function postStationSwap(
  stationId: string,
  incomingBatteryId: string,
): Promise<StationSwapResult> {
  const base = apiBase();
  try {
    const raw = await fetchJson<Record<string, unknown>>(
      `${base}/stations/${encodeURIComponent(stationId)}/swaps`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ incomingBatteryId }),
        timeoutMs: TIMEOUT_MS,
      },
    );
    return {
      stationId: String(raw.stationId ?? stationId),
      outgoingId: String(raw.outgoingId ?? ""),
      incomingId: String(raw.incomingId ?? ""),
    };
  } catch (e) {
    throw triageStationError(e);
  }
}
