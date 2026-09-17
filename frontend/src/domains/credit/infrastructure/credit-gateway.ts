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
} from "@/lib/credit/types";
import { fetchJson } from "@/shared/http/fetch-json";

export const DEFAULT_CREDIT_USER = "U1";

const TIMEOUT_MS = 8000;

/**
 * Server Component 无相对 URL host；直连 BACKEND_ORIGIN。
 * 客户端仍走同域 `/api` → rewrite。
 */
function resolveCreditApiBase(): string {
  if (typeof window === "undefined") {
    return process.env.BACKEND_ORIGIN ?? "http://localhost:8080";
  }
  return process.env.NEXT_PUBLIC_API_BASE ?? "/api";
}

export async function fetchCreditProfile(
  userId: string = DEFAULT_CREDIT_USER,
): Promise<CreditProfile> {
  const base = resolveCreditApiBase();
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
  const base = resolveCreditApiBase();
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

export type CreditRepayRequest = {
  userId: string;
  statementId: string;
  amountCents: number;
};

/** POST /credit/profiles/{userId}/repay — 错误经 fetchJson 已拼 suggestion */
export async function postCreditRepay(
  req: CreditRepayRequest,
): Promise<unknown> {
  const base = resolveCreditApiBase();
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
