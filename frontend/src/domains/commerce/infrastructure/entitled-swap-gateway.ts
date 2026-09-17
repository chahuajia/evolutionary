/**
 * 权益换电防腐层 — HTTP gateway（DTO → 前端读模型）
 * RSC 直连 Spring；浏览器经 Next `/api` rewrite。
 * 与站级 swap 并行；BE 未就绪时 POST 可 404（压测可接受）。
 */

import { fetchJson } from "@/shared/http/fetch-json";

export type EntitledSwapRequest = {
  userId: string;
  entitlementId: string;
  cabinetId: string;
};

export type EntitledSwapResult = {
  id: string;
  userId: string;
  entitlementId: string;
  batteryId: string;
  cabinetId: string;
  status: string;
};

const TIMEOUT_MS = 8000;

/**
 * Server Component 无相对 URL host；直连 BACKEND_ORIGIN。
 * 客户端仍走同域 `/api` → rewrite。
 */
export function resolveApiBase(): string {
  if (typeof window === "undefined") {
    return process.env.BACKEND_ORIGIN ?? "http://localhost:8080";
  }
  return process.env.NEXT_PUBLIC_API_BASE ?? "/api";
}

function triageSwapError(e: unknown): Error {
  const msg = e instanceof Error ? e.message : String(e);
  if (msg === "Failed to fetch" || msg.includes("NetworkError")) {
    return new Error(
      "权益换电失败（W1）：请确认 Spring 已启动 :8080，且 Next rewrite /api 生效。",
    );
  }
  if (msg.startsWith("HTTP ")) {
    return new Error(`权益换电失败（${msg}）`);
  }
  return e instanceof Error ? e : new Error(msg);
}

function parseEntitledSwapResult(
  raw: Record<string, unknown>,
): EntitledSwapResult {
  return {
    id: String(raw.id ?? ""),
    userId: String(raw.userId ?? ""),
    entitlementId: String(raw.entitlementId ?? ""),
    batteryId: String(raw.batteryId ?? ""),
    cabinetId: String(raw.cabinetId ?? ""),
    status: String(raw.status ?? ""),
  };
}

/** POST /entitled-swaps；8s 超时。 */
export async function postEntitledSwap(
  req: EntitledSwapRequest,
): Promise<EntitledSwapResult> {
  const base = resolveApiBase();
  try {
    const raw = await fetchJson<Record<string, unknown>>(
      `${base}/entitled-swaps`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId: req.userId,
          entitlementId: req.entitlementId,
          cabinetId: req.cabinetId,
        }),
        timeoutMs: TIMEOUT_MS,
      },
    );
    return parseEntitledSwapResult(raw);
  } catch (e) {
    throw triageSwapError(e);
  }
}
