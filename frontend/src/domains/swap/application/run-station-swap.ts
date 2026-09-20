/**
 * 用例：站内换电（编排 gateway → 换电结果）。
 */

import {
  postStationSwap,
  type StationSwapResult,
} from "@/domains/swap/infrastructure/station-gateway";

export async function runStationSwap(input: {
  stationId: string;
  incomingBatteryId: string;
}): Promise<StationSwapResult> {
  return postStationSwap(input.stationId, input.incomingBatteryId);
}

export type { StationSwapResult };
