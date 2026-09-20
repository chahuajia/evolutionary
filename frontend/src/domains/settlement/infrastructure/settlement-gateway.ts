/**
 * 结算防腐 — POST /settlement/batches · /settlement/accruals
 * 契约对齐 21a：orgId / amountCents；batch 字段 id。
 */

import { parseAccrualStatus } from "@/domains/settlement/domain/accrual-view";
import { apiBase } from "@/shared/http/api-base";
import { fetchJson } from "@/shared/http/fetch-json";
import type { AccrualStatus } from "@/domains/settlement/domain/accrual-view";

const TIMEOUT_MS = 8000;

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

/**
 * 线格式（wire DTO）—— 网关只负责把服务端 JSON 变成这个形状。
 *
 * **不是 `AccrualView`。** 这里原先自己定义了一份同名的 `AccrualView`
 * （`status: string`），与 `domain/accrual-view.ts` 的那份**并行存在** ——
 * 两份真相必然漂移（[[patterns/derivation-over-copy]]），
 * 实际漂出的就是测试里的 "ACCRUED"（后端从来不存在）。
 *
 * 现在：类型从领域**派生**（`AccrualStatus`），视图转换仍归 `toAccrualView`。
 */
export type AccrualDto = {
  id: string;
  orderId: string;
  orgId: string;
  amountCents: number;
  status: AccrualStatus;
};

/** POST /settlement/batches */
export async function postRunSettlementBatch(req: {
  periodStart: string;
  periodEnd: string;
}): Promise<SettlementBatchResult> {
  const base = apiBase();
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
): Promise<AccrualDto[]> {
  const base = apiBase();
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
  return (Array.isArray(raw) ? raw : []).map(toAccrualDto);
}

/**
 * `GET /settlement/accruals?orgId=` —— 读侧。
 *
 * @remarks
 * 与上面几个 POST 的区别是**只读**：不碰任何用例。
 * 存在的理由见后端 `SettlementController.listAccruals` ——
 * 只返 PENDING 的话，界面上永远没有"为什么这条不能动"需要解释的东西，
 * 展示不变量（`canSettleAccrual` 等）就成了死代码。
 */
export async function fetchAccruals(orgId: string): Promise<AccrualDto[]> {
  const base = apiBase();
  const raw = await fetchJson<unknown[]>(
    `${base}/settlement/accruals?orgId=${encodeURIComponent(orgId)}`,
    { method: "GET", timeoutMs: TIMEOUT_MS },
  );
  return (Array.isArray(raw) ? raw : []).map(toAccrualDto);
}

/** GET /settlement/accruals?orderId= — 冲销门按订单真态 */
export async function fetchAccrualsByOrderId(
  orderId: string,
): Promise<AccrualDto[]> {
  const id = orderId.trim();
  if (!id) {
    throw new Error("orderId required");
  }
  const base = apiBase();
  const raw = await fetchJson<unknown[]>(
    `${base}/settlement/accruals?orderId=${encodeURIComponent(id)}`,
    { method: "GET", timeoutMs: TIMEOUT_MS },
  );
  return (Array.isArray(raw) ? raw : []).map(toAccrualDto);
}

/** POST /settlement/orders/{orderId}/reverse-accruals（AC-35） */
export async function postReverseAccruals(
  orderId: string,
): Promise<AccrualDto[]> {
  const base = apiBase();
  const raw = await fetchJson<unknown[]>(
    `${base}/settlement/orders/${encodeURIComponent(orderId)}/reverse-accruals`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      timeoutMs: TIMEOUT_MS,
    },
  );
  return (Array.isArray(raw) ? raw : []).map(toAccrualDto);
}

/** 线格式 → DTO。**边界解析，不是强转**（parse-dont-validate）。 */
function toAccrualDto(row: unknown): AccrualDto {
  const r = row as Record<string, unknown>;
  return {
    id: String(r.id ?? ""),
    orderId: String(r.orderId ?? ""),
    orgId: String(r.orgId ?? ""),
    amountCents: typeof r.amountCents === "number" ? r.amountCents : 0,
    status: parseAccrualStatus(r.status),
  };
}
