/**
 * 用例：加载单笔信用账单展示模型（编排 gateway → BillingStatementView）。
 */

import {
  toBillingStatementView,
  type BillingStatementView,
} from "@/domains/credit/domain/billing-statement-view";
import { fetchCreditStatement } from "@/domains/credit/infrastructure/credit-gateway";

export async function loadCreditStatement(
  statementId: string,
): Promise<BillingStatementView> {
  const row = await fetchCreditStatement(statementId);
  return toBillingStatementView({
    id: row.id,
    userId: row.userId,
    status: row.status,
    totalDue: row.totalDue,
    periodStart: row.periodStart,
    periodEnd: row.periodEnd,
    dueDate: row.dueDate,
    paidAt: row.paidAt,
  });
}
