/**
 * 信用 BC 防腐层 — HTTP gateway（DTO → 前端读模型）
 * RSC 直连 Spring；浏览器经 Next `/api` rewrite。
 */

import {
  isCreditStatus,
  isStatementStatus,
  type BillingStatement,
  type CreditProfile,
  type ScoreTier,
} from "@/domains/credit/domain/credit-contracts";
import { apiBase } from "@/shared/http/api-base";
import { fetchJson } from "@/shared/http/fetch-json";

export const DEFAULT_CREDIT_USER = "U1";

const TIMEOUT_MS = 8000;

export async function fetchCreditProfile(
  userId: string = DEFAULT_CREDIT_USER,
): Promise<CreditProfile> {
  const base = apiBase();
  let raw: Record<string, unknown>;
  try {
    raw = await fetchJson<Record<string, unknown>>(
      `${base}/credit/profiles/${encodeURIComponent(userId)}`,
      { cache: "no-store", timeoutMs: TIMEOUT_MS },
    );
  } catch (e) {
    const msg = e instanceof Error ? e.message : String(e);
    if (msg === "HTTP 404") {
      throw new Error(`信用档案不存在：${userId}`);
    }
    if (msg.startsWith("HTTP ")) {
      throw new Error(`信用档案拉取失败（${msg}）`);
    }
    throw e instanceof Error ? e : new Error(msg);
  }
  return parseProfile(raw);
}

export async function fetchCreditStatements(
  userId: string = DEFAULT_CREDIT_USER,
): Promise<readonly BillingStatement[]> {
  const base = apiBase();
  let raw: unknown[];
  try {
    raw = await fetchJson<unknown[]>(
      `${base}/credit/profiles/${encodeURIComponent(userId)}/statements`,
      { cache: "no-store", timeoutMs: TIMEOUT_MS },
    );
  } catch (e) {
    const msg = e instanceof Error ? e.message : String(e);
    if (msg.startsWith("HTTP ")) {
      throw new Error(`账单列表拉取失败（${msg}）`);
    }
    throw e instanceof Error ? e : new Error(msg);
  }
  return raw.map((row) => parseStatement(row as Record<string, unknown>));
}

/** GET /credit/statements/{statementId} — 单账单真态（repayAllowed） */
export async function fetchCreditStatement(
  statementId: string,
): Promise<BillingStatement> {
  const id = statementId.trim();
  if (!id) {
    throw new Error("statementId required");
  }
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/credit/statements/${encodeURIComponent(id)}`,
    { cache: "no-store", timeoutMs: TIMEOUT_MS },
  );
  return parseStatement(raw);
}

export type CreditRepayRequest = {
  userId: string;
  statementId: string;
  amountCents: number;
};

/** POST /credit/profiles/{userId}/repay — 错误经 fetchJson 已拼 suggestion */
export async function postCreditRepay(
  req: CreditRepayRequest,
): Promise<unknown> {
  const base = apiBase();
  const { userId, statementId, amountCents } = req;
  return fetchJson(
    `${base}/credit/profiles/${encodeURIComponent(userId)}/repay`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ statementId, amountCents }),
      timeoutMs: TIMEOUT_MS,
    },
  );
}

export type MarkOverdueRequest = {
  userId: string;
  statementId: string;
};

/** POST /credit/profiles/{userId}/mark-overdue — 仅 DUE 账单；返回更新后档案 */
export async function postMarkCreditOverdue(
  req: MarkOverdueRequest,
): Promise<CreditProfile> {
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/credit/profiles/${encodeURIComponent(req.userId)}/mark-overdue`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ statementId: req.statementId }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseProfile(raw);
}

export type CreditPurchaseRequest = {
  userId: string;
  productId: string;
};

/** 信用购成功读模型 — 对齐 POST /credit/purchases 200（orderId/entitlementId/…） */
export type CreditPurchaseResult = {
  orderId: string;
  entitlementId: string;
  productId?: string;
  userId?: string;
  paidAmountCents?: number;
  debtId?: string;
};

/** POST /credit/purchases — 错误经 fetchJson 已拼 suggestion */
export async function postCreditPurchase(
  req: CreditPurchaseRequest,
): Promise<CreditPurchaseResult> {
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/credit/purchases`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        userId: req.userId,
        productId: req.productId,
      }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parsePurchaseResult(raw);
}

export type MonthlyBillingRequest = {
  userId: string;
  /** 账期起：YYYY-MM-DD 或 ISO-8601 Instant */
  periodStart: string;
  /** 账期止：YYYY-MM-DD 或 ISO-8601 Instant */
  periodEnd: string;
};

/** 日期/Instant → Spring Instant.parse 可接受的 ISO-8601 */
function toIsoInstant(raw: string, field: string): string {
  const v = raw.trim();
  if (!v) throw new Error(`${field} required`);
  if (/^\d{4}-\d{2}-\d{2}$/.test(v)) return `${v}T00:00:00Z`;
  // 已是 Instant / 带偏移的日期时间则原样提交，由后端校验
  return v;
}

/** POST /credit/profiles/{userId}/monthly-billing — 返回新账单 */
export async function postMonthlyBilling(
  req: MonthlyBillingRequest,
): Promise<BillingStatement> {
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/credit/profiles/${encodeURIComponent(req.userId)}/monthly-billing`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        periodStart: toIsoInstant(req.periodStart, "periodStart"),
        periodEnd: toIsoInstant(req.periodEnd, "periodEnd"),
      }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseStatement(raw);
}

export type ApplyCreditPolicyRequest = {
  userId: string;
  policyVersion: number;
};

/** POST /credit/profiles/{userId}/apply-policy — 返回更新后档案读模型 */
export async function postApplyCreditPolicy(
  req: ApplyCreditPolicyRequest,
): Promise<CreditProfile> {
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/credit/profiles/${encodeURIComponent(req.userId)}/apply-policy`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ policyVersion: req.policyVersion }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseProfile(raw);
}

function parsePurchaseResult(raw: Record<string, unknown>): CreditPurchaseResult {
  const order =
    raw.order != null && typeof raw.order === "object"
      ? (raw.order as Record<string, unknown>)
      : null;
  const entitlement =
    raw.entitlement != null && typeof raw.entitlement === "object"
      ? (raw.entitlement as Record<string, unknown>)
      : null;
  const orderId = String(raw.orderId ?? order?.id ?? "");
  const entitlementId = String(raw.entitlementId ?? entitlement?.id ?? "");
  if (!orderId || !entitlementId) {
    throw new Error("信用购响应缺少 orderId/entitlementId");
  }
  const paidRaw = raw.paidAmountCents ?? order?.paidAmountCents;
  const debt =
    raw.debt != null && typeof raw.debt === "object"
      ? (raw.debt as Record<string, unknown>)
      : null;
  return {
    orderId,
    entitlementId,
    productId:
      raw.productId != null
        ? String(raw.productId)
        : order?.productId != null
          ? String(order.productId)
          : undefined,
    userId:
      raw.userId != null
        ? String(raw.userId)
        : order?.userId != null
          ? String(order.userId)
          : undefined,
    paidAmountCents:
      paidRaw != null && paidRaw !== "" ? Number(paidRaw) : undefined,
    debtId:
      raw.debtId != null
        ? String(raw.debtId)
        : debt?.id != null
          ? String(debt.id)
          : undefined,
  };
}

function parseProfile(raw: Record<string, unknown>): CreditProfile {
  const status = String(raw.status ?? "");
  if (!isCreditStatus(status)) {
    throw new Error(`未知信用状态: ${status}`);
  }
  const tier = String(raw.scoreTier ?? "A") as ScoreTier;
  return {
    userId: String(raw.userId),
    creditLimit: Number(raw.creditLimit),
    usedCredit: Number(raw.usedCredit),
    status,
    scoreTier: tier,
    policyVersion: Number(raw.policyVersion),
  };
}

function parseStatement(raw: Record<string, unknown>): BillingStatement {
  const status = String(raw.status ?? "");
  if (!isStatementStatus(status)) {
    throw new Error(`未知账单状态: ${status}`);
  }
  return {
    id: String(raw.id),
    userId: String(raw.userId),
    periodStart: String(raw.periodStart),
    periodEnd: String(raw.periodEnd),
    totalDue: Number(raw.totalDue),
    status,
    dueDate: String(raw.dueDate),
    createdAt: String(raw.createdAt),
    paidAt: raw.paidAt == null ? undefined : String(raw.paidAt),
  };
}
