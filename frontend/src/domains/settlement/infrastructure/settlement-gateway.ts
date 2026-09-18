/**
 * 结算防腐 — POST /settlement/batches · /settlement/accruals
 * 契约对齐 21a：orgId / amountCents；batch 字段 id。
 */

import { fetchJson } from "@/shared/http/fetch-json";

const TIMEOUT_MS = 8000;

function resolveApiBase(): string {
  if (typeof window === "undefined") {
    return process.env.BACKEND_ORIGIN ?? "http://localhost:8080";
  }
  return process.env.NEXT_PUBLIC_API_BASE ?? "/api";
}

export type SettlementBatchResult = {
  id: string;
  status: string;
  periodStart: string;
  periodEnd: string;
  closedAt?: string | null;
};

export type AccrueRequest = {
  orderId: string;
  orgId: string;
  amountCents: number;
  userId?: string;
  currency?: string;
  completedAt?: string;
};

export type AccrualView = {
  id: string;
  orderId: string;
  orgId: string;
  amountCents: number;
  status: string;
};

/** POST /settlement/batches */
export async function postRunSettlementBatch(req: {
  periodStart: string;
  periodEnd: string;
}): Promise<SettlementBatchResult> {
  const base = resolveApiBase();
  const raw = await fetchJson<Record<string, unknown>>(`${base}/settlement/batches`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      periodStart: req.periodStart,
      periodEnd: req.periodEnd,
    }),
    timeoutMs: TIMEOUT_MS,
  });
  return {
    id: String(raw.id ?? raw.batchId ?? ""),
    status: String(raw.status ?? ""),
    periodStart: String(raw.periodStart ?? ""),
    periodEnd: String(raw.periodEnd ?? ""),
    closedAt: raw.closedAt != null ? String(raw.closedAt) : null,
  };
}

/** POST /settlement/accruals */
export async function postAccrueSettlement(
  req: AccrueRequest,
): Promise<AccrualView[]> {
  const base = resolveApiBase();
  const raw = await fetchJson<unknown[]>(`${base}/settlement/accruals`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      orderId: req.orderId,
      orgId: req.orgId,
      amountCents: req.amountCents,
      userId: req.userId,
      currency: req.currency ?? "CNY",
      completedAt: req.completedAt,
    }),
    timeoutMs: TIMEOUT_MS,
  });
  return (Array.isArray(raw) ? raw : []).map((row) => {
    const r = row as Record<string, unknown>;
    return {
      id: String(r.id ?? ""),
      orderId: String(r.orderId ?? ""),
      orgId: String(r.orgId ?? ""),
      amountCents: typeof r.amountCents === "number" ? r.amountCents : 0,
      status: String(r.status ?? ""),
    };
  });
}
