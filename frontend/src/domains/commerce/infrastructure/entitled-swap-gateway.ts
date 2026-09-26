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

export type EntitlementDto = {
  id: string;
  userId: string;
  status: string;
  remainingSwaps: number | null;
  productId: string | null;
  /** 计量费率（分 / SOC）；非计量为 null */
  meteredRateCents: number | null;
};

function parseEntitlementDto(
  raw: Record<string, unknown>,
  fallbackId = "",
): EntitlementDto {
  const remaining = raw.remainingSwaps;
  const rate = raw.meteredRateCents;
  return {
    id: String(raw.id ?? fallbackId),
    userId: String(raw.userId ?? ""),
    status: String(raw.status ?? ""),
    remainingSwaps:
      typeof remaining === "number" && Number.isFinite(remaining)
        ? remaining
        : null,
    productId:
      raw.productId == null || String(raw.productId) === ""
        ? null
        : String(raw.productId),
    meteredRateCents:
      typeof rate === "number" && Number.isFinite(rate) ? rate : null,
  };
}

/**
 * GET /entitled-swaps?userId= — ACTIVE 目录（AC-14 预览）。
 */
export async function fetchActiveEntitlements(
  userId: string,
): Promise<EntitlementDto[]> {
  const id = userId.trim();
  const base = apiBase();
  const raw = await fetchJson<unknown>(
    `${base}/entitled-swaps?userId=${encodeURIComponent(id)}`,
    { timeoutMs: TIMEOUT_MS },
  );
  if (!Array.isArray(raw)) {
    throw new Error("权益目录响应非数组");
  }
  return raw.map((row) =>
    parseEntitlementDto(
      typeof row === "object" && row != null
        ? (row as Record<string, unknown>)
        : {},
    ),
  );
}

/**
 * GET /entitled-swaps/{entitlementId}
 */
export async function fetchEntitlement(
  entitlementId: string,
): Promise<EntitlementDto> {
  const id = entitlementId.trim();
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/entitled-swaps/${encodeURIComponent(id)}`,
    { timeoutMs: TIMEOUT_MS },
  );
  return parseEntitlementDto(raw, id);
}

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
