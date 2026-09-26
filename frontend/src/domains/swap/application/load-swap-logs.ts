/**
 * 用例：加载站换电日志（编排 gateway → SwapLogView）。
 */

import {
  toSwapLogView,
  type SwapLogView,
} from "@/domains/swap/domain/swap-log-view";
import { fetchSwapLogs } from "@/domains/swap/infrastructure/station-gateway";

export type { SwapLogView };

export async function loadSwapLogs(
  stationId: string,
): Promise<readonly SwapLogView[]> {
  const rows = await fetchSwapLogs(stationId);
  return rows.map(toSwapLogView);
}
