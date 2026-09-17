/**
 * 权益换电防腐 — POST /entitled-swaps
 */

import { fetchJson } from "@/shared/http/fetch-json";

const TIMEOUT_MS = 8000;

export type EntitledSwapRequest = {
  userId: string;
  entitlementId: string;
  cabinetId: string;
};

export type EntitledSwapResult = {
  usageEventId: string;
  status: string;
  batteryId: string;
  cabinetId: string;
  entitlementId: string;
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
