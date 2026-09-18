/**
 * 权益换电防腐 — POST /entitled-swaps
 */

import { apiBase } from "@/shared/http/api-base";
import { fetchJson } from "@/shared/http/fetch-json";

const TIMEOUT_MS = 8000;

export type EntitledSwapRequest = {
  userId: string;
  /** 省略则 BE 默认选卡（AC-14） */
  entitlementId?: string;
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

export async function postEntitledSwap(
  body: EntitledSwapRequest,
): Promise<EntitledSwapResult> {
  const base = apiBase();
  const payload: Record<string, unknown> = {
    userId: body.userId,
    cabinetId: body.cabinetId,
  };
  if (body.entitlementId != null && body.entitlementId.trim() !== "") {
    payload.entitlementId = body.entitlementId.trim();
  }
  if (body.socBefore != null) payload.socBefore = body.socBefore;
  if (body.socAfter != null) payload.socAfter = body.socAfter;

  return fetchJson<EntitledSwapResult>(`${base}/entitled-swaps`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
    timeoutMs: TIMEOUT_MS,
  });
}
