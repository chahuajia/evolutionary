/**
 * 权益换电防腐 — POST /entitled-swaps
 */

import { fetchJson } from "@/shared/http/fetch-json";

const TIMEOUT_MS = 8000;

export type EntitledSwapRequest = {
  userId: string;
  entitlementId: string;
  cabinetId: string;
  /** 计量权益换电：换前 SOC（可选） */
  socBefore?: number;
  /** 计量权益换电：换后 SOC（可选） */
  socAfter?: number;
};

export type EntitledSwapResult = {
  usageEventId: string;
  status: string;
  batteryId: string;
  cabinetId: string;
  entitlementId: string;
  /** 计量扣费（分）；非计量权益可能缺省 */
  chargedAmountCents?: number;
};

function resolveApiBase(): string {
  if (typeof window === "undefined") {
    return process.env.BACKEND_ORIGIN ?? "http://localhost:8080";
  }
  return process.env.NEXT_PUBLIC_API_BASE ?? "/api";
}

export async function postEntitledSwap(
  body: EntitledSwapRequest,
): Promise<EntitledSwapResult> {
  const base = resolveApiBase();
  return fetchJson<EntitledSwapResult>(`${base}/entitled-swaps`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
    timeoutMs: TIMEOUT_MS,
  });
}
