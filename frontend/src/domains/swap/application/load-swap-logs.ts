/**
 * 用例：加载站换电日志（编排 gateway，不写 fetch 细节）。
 */

import {
  fetchSwapLogs,
  type SwapLog,
} from "@/domains/swap/infrastructure/station-gateway";

export async function loadSwapLogs(
  stationId: string,
): Promise<readonly SwapLog[]> {
  return fetchSwapLogs(stationId);
}

export type { SwapLog };
