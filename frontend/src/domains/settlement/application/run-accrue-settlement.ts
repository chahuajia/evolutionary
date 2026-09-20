/**
 * 用例：记分润意向（编排 gateway → AccrualView[]）。
 */

import {
  toAccrualView,
  type AccrualView,
} from "@/domains/settlement/domain/accrual-view";
import {
  postAccrueSettlement,
  type AccrueRequest,
} from "@/domains/settlement/infrastructure/settlement-gateway";

export async function runAccrueSettlement(
  req: AccrueRequest,
): Promise<AccrualView[]> {
  const rows = await postAccrueSettlement(req);
  return rows.map(toAccrualView);
}
