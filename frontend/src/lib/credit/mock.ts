/**
 * 信用账单 mock — 对齐 AC-48..54 fixture 文案级（U1 limit 100 / used 30）
 */

import type { BillingStatement, CreditProfile } from "./types";

export const MOCK_CREDIT_PROFILE: CreditProfile = {
  userId: "U1",
  creditLimit: 10_000,
  usedCredit: 3_000,
  status: "good",
  scoreTier: "A",
  policyVersion: 1,
};

/** 含 DUE / PAID，覆盖出账与还款后只读展示 */
export const MOCK_STATEMENTS: readonly BillingStatement[] = [
  {
    id: "STMT-2026-02",
    userId: "U1",
    periodStart: "2026-01-01",
    periodEnd: "2026-01-31",
    totalDue: 3_000,
    status: "DUE",
    dueDate: "2026-02-15",
    createdAt: "2026-02-01T00:00:00Z",
  },
  {
    id: "STMT-2025-12",
    userId: "U1",
    periodStart: "2025-11-01",
    periodEnd: "2025-11-30",
    totalDue: 2_500,
    status: "PAID",
    dueDate: "2025-12-15",
    createdAt: "2025-12-01T00:00:00Z",
    paidAt: "2025-12-10T08:30:00Z",
  },
];
