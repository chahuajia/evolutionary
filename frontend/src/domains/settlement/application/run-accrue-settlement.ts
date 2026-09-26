/**
 * 用例：记分润意向（编排 gateway → AccrualView[]）。
 */

import {
  toAccrualView,
  type AccrualView,
} from "@/domains/settlement/domain/accrual-view";
import { postAccrueSettlement } from "@/domains/settlement/infrastructure/settlement-gateway";

export type AccrueSettlementInput = {
  readonly orderId: string;
  readonly orgId: string;
  readonly amountCents: number;
  readonly userId?: string;
  readonly currency?: string;
  readonly completedAt?: string;
};

export async function runAccrueSettlement(
  req: AccrueSettlementInput,
): Promise<AccrualView[]> {
  const rows = await postAccrueSettlement({
    orderId: req.orderId,
    orgId: req.orgId,
    amountCents: req.amountCents,
    userId: req.userId,
    currency: req.currency,
    completedAt: req.completedAt,
  });
  return rows.map(toAccrualView);
}
