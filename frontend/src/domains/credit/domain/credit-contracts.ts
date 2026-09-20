/**
 * 信用域契约类型 — 字段名对齐 phase-6 IDL（contracts/phase-6.ts）。
 * 网关 / 视图边界共用；展示不变量仍在 *-view.ts。
 */

import { formatCentsAsYuan } from "@/shared/money/format-cents";

export type MoneyCents = number;
export type UserId = string;
export type StatementId = string;

export const CreditStatusValues = {
  Good: "good",
  Overdue: "overdue",
  Frozen: "frozen",
} as const;
export type CreditStatus =
  (typeof CreditStatusValues)[keyof typeof CreditStatusValues];

export type ScoreTier = "A" | "B" | "C";

/** 用户信用档案（HTTP 读模型） */
export interface CreditProfile {
  readonly userId: UserId;
  readonly creditLimit: MoneyCents;
  readonly usedCredit: MoneyCents;
  readonly status: CreditStatus;
  readonly scoreTier: ScoreTier;
  readonly policyVersion: number;
}

export const StatementStatusValues = {
  Open: "OPEN",
  Due: "DUE",
  Paid: "PAID",
  Overdue: "OVERDUE",
} as const;
export type StatementStatus =
  (typeof StatementStatusValues)[keyof typeof StatementStatusValues];

/** 月度账单（HTTP 读模型） */
export interface BillingStatement {
  readonly id: StatementId;
  readonly userId: UserId;
  readonly periodStart: string;
  readonly periodEnd: string;
  readonly totalDue: MoneyCents;
  readonly status: StatementStatus;
  readonly dueDate: string;
  readonly createdAt: string;
  readonly paidAt?: string;
}

/** UI 文案映射 */
export const CREDIT_STATUS_LABEL: Record<CreditStatus, string> = {
  good: "正常",
  overdue: "逾期",
  frozen: "冻结",
};

export const STATEMENT_STATUS_LABEL: Record<StatementStatus, string> = {
  OPEN: "未出账",
  DUE: "待还",
  PAID: "已还",
  OVERDUE: "逾期",
};

/** @deprecated 用 shared `formatCentsAsYuan`；保留别名以免旧 import 断裂 */
export function formatYuan(cents: MoneyCents): string {
  return formatCentsAsYuan(cents);
}

export function isCreditStatus(v: string): v is CreditStatus {
  return v === "good" || v === "overdue" || v === "frozen";
}

export function isStatementStatus(v: string): v is StatementStatus {
  return v === "OPEN" || v === "DUE" || v === "PAID" || v === "OVERDUE";
}
