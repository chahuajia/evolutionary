/**
 * 用例：冲销订单意向（编排 gateway → AccrualView[]）。
 */

import {
  toAccrualView,
  type AccrualView,
} from "@/domains/settlement/domain/accrual-view";
import { postReverseAccruals } from "@/domains/settlement/infrastructure/settlement-gateway";

export async function runReverseAccruals(
  orderId: string,
): Promise<AccrualView[]> {
  const rows = await postReverseAccruals(orderId);
  return rows.map(toAccrualView);
}
