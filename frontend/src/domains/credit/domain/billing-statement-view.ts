/**
 * 账单展示模型 — 视图边界。
 *
 * 对齐后端 `BillingStatement`：
 * - markPaid / repay：仅 DUE / OVERDUE
 * - markOverdue：仅 DUE
 * - isPastDue：DUE|OVERDUE 且 now > dueDate（展示提示，非动作门）
 */

import { formatCentsAsYuan } from "@/shared/money/format-cents";

export type BillingStatementStatus = "OPEN" | "DUE" | "PAID" | "OVERDUE";

const STATEMENT_STATUSES = ["OPEN", "DUE", "PAID", "OVERDUE"] as const;

export function parseBillingStatementStatus(
  raw: unknown,
): BillingStatementStatus {
  if (
    typeof raw === "string" &&
    (STATEMENT_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as BillingStatementStatus;
  }
  throw new Error(
    `未知账单状态：${String(raw)}（契约：${STATEMENT_STATUSES.join(" | ")}）`,
  );
}

export const BILLING_STATEMENT_STATUS_LABEL: Record<
  BillingStatementStatus,
  string
> = {
  OPEN: "未出账",
  DUE: "待还",
  PAID: "已还",
  OVERDUE: "逾期",
};

/** 徽章色调：页面只映射 tone→CSS，勿再 status===。 */
export type BillingStatementBadgeTone =
  | "due"
  | "paid"
  | "overdue"
  | "neutral";

export function billingStatementBadgeTone(
  status: BillingStatementStatus,
): BillingStatementBadgeTone {
  if (status === "DUE") return "due";
  if (status === "PAID") return "paid";
  if (status === "OVERDUE") return "overdue";
  return "neutral";
}

export type BillingStatementView = {
  readonly id: string;
  readonly userId: string;
  readonly status: BillingStatementStatus;
  readonly statusLabel: string;
  readonly badgeTone: BillingStatementBadgeTone;
  readonly totalDueCents: number;
  readonly totalDueYuan: string;
  readonly periodStart: string;
  readonly periodEnd: string;
  readonly dueDate: string;
  readonly paidAt: string | null;
  /** 仅 DUE / OVERDUE 可还款（对齐 markPaid）。 */
  readonly repayAllowed: boolean;
  /** 仅 DUE 可标逾期（对齐 markOverdue）。 */
  readonly markOverdueAllowed: boolean;
  readonly blockMessage: string | null;
};

/** 展示不变量：仅 DUE / OVERDUE 可还款。 */
export function canRepayStatement(status: BillingStatementStatus): boolean {
  return status === "DUE" || status === "OVERDUE";
}

/** 展示不变量：仅 DUE 可标逾期。 */
export function canMarkStatementOverdue(
  status: BillingStatementStatus,
): boolean {
  return status === "DUE";
}

export function billingStatementBlockMessage(
  status: BillingStatementStatus,
): string | null {
  if (status === "DUE" || status === "OVERDUE") return null;
  if (status === "PAID") return "账单已还清，不可再还";
  return "账单未出账，不可还款";
}

export function toBillingStatementView(dto: {
  id: string;
  userId: string;
  status: string;
  totalDue: number;
  periodStart: string;
  periodEnd: string;
  dueDate: string;
  paidAt?: string | null;
}): BillingStatementView {
  const status = parseBillingStatementStatus(dto.status);
  return {
    id: dto.id,
    userId: dto.userId,
    status,
    statusLabel: BILLING_STATEMENT_STATUS_LABEL[status],
    badgeTone: billingStatementBadgeTone(status),
    totalDueCents: dto.totalDue,
    totalDueYuan: formatCentsAsYuan(dto.totalDue),
    periodStart: dto.periodStart,
    periodEnd: dto.periodEnd,
    dueDate: dto.dueDate,
    paidAt: dto.paidAt ?? null,
    repayAllowed: canRepayStatement(status),
    markOverdueAllowed: canMarkStatementOverdue(status),
    blockMessage: billingStatementBlockMessage(status),
  };
}
