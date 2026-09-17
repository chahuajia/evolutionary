/**
 * 信用 API 客户端 — Next rewrite `/api` → Spring :8080
 */

import {
  isCreditStatus,
  isStatementStatus,
  type BillingStatement,
  type CreditProfile,
  type ScoreTier,
} from "./types";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "/api";

export const DEFAULT_CREDIT_USER = "U1";

export async function fetchCreditProfile(
  userId: string = DEFAULT_CREDIT_USER,
): Promise<CreditProfile> {
  const res = await fetch(
    `${API_BASE}/credit/profiles/${encodeURIComponent(userId)}`,
    { cache: "no-store" },
  );
  if (!res.ok) {
    throw new Error(
      res.status === 404
        ? `信用档案不存在：${userId}`
        : `信用档案拉取失败（HTTP ${res.status}）`,
    );
  }
  const raw = (await res.json()) as Record<string, unknown>;
  return parseProfile(raw);
}

export async function fetchCreditStatements(
  userId: string = DEFAULT_CREDIT_USER,
): Promise<readonly BillingStatement[]> {
  const res = await fetch(
    `${API_BASE}/credit/profiles/${encodeURIComponent(userId)}/statements`,
    { cache: "no-store" },
  );
  if (!res.ok) {
    throw new Error(`账单列表拉取失败（HTTP ${res.status}）`);
  }
  const raw = (await res.json()) as unknown[];
  return raw.map((row) => parseStatement(row as Record<string, unknown>));
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
