/**
 * 用例：跑结算批（编排 gateway → SettlementBatchView）。
 */

import {
  toSettlementBatchView,
  type SettlementBatchView,
} from "@/domains/settlement/domain/settlement-batch-view";
import { postRunSettlementBatch } from "@/domains/settlement/infrastructure/settlement-gateway";

export async function runSettlementBatch(input: {
  periodStart: string;
  periodEnd: string;
}): Promise<SettlementBatchView> {
  const r = await postRunSettlementBatch(input);
  return toSettlementBatchView(r);
}
