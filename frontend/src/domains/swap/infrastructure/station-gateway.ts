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

const TIMEOUT_MS = 8000;

/** @deprecated 请用 `@/shared/http/api-base`；保留别名供 swap-panel 兼容 */
export const resolveApiBase = apiBase;

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
