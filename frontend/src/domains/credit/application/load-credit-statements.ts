/**
 * 用例：加载信用账单展示模型（编排 gateway，不写 fetch 细节）。
 */

import {
  toBillingStatementView,
  type BillingStatementView,
} from "@/domains/credit/domain/billing-statement-view";
import {
  DEFAULT_CREDIT_USER,
  fetchCreditStatements,
} from "@/domains/credit/infrastructure/credit-gateway";

export async function loadCreditStatements(
  userId: string = DEFAULT_CREDIT_USER,
): Promise<BillingStatementView[]> {
  const rows = await fetchCreditStatements(userId);
  return rows.map((row) =>
    toBillingStatementView({
      id: row.id,
      userId: row.userId,
      status: row.status,
      totalDue: row.totalDue,
      periodStart: row.periodStart,
      periodEnd: row.periodEnd,
      dueDate: row.dueDate,
      paidAt: row.paidAt,
    }),
  );
}
