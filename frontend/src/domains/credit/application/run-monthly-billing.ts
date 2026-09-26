/**
 * 用例：月度出账（编排 gateway → BillingStatementView）。
 */

import {
  toBillingStatementView,
  type BillingStatementView,
} from "@/domains/credit/domain/billing-statement-view";
import {
  DEFAULT_CREDIT_USER,
  postMonthlyBilling,
} from "@/domains/credit/infrastructure/credit-gateway";

export async function runMonthlyBilling(input: {
  userId?: string;
  periodStart: string;
  periodEnd: string;
}): Promise<BillingStatementView> {
  const statement = await postMonthlyBilling({
    userId: input.userId?.trim() || DEFAULT_CREDIT_USER,
    periodStart: input.periodStart,
    periodEnd: input.periodEnd,
  });
  return toBillingStatementView({
    id: statement.id,
    userId: statement.userId,
    status: statement.status,
    totalDue: statement.totalDue,
    periodStart: statement.periodStart,
    periodEnd: statement.periodEnd,
    dueDate: statement.dueDate,
    paidAt: statement.paidAt,
  });
}

export { DEFAULT_CREDIT_USER };
