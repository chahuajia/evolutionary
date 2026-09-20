/**
 * 用例：站内换电（编排 gateway → StationSwapResultView）。
 */

import {
  toStationSwapResultView,
  type StationSwapResultView,
} from "@/domains/swap/domain/station-swap-result-view";
import { postStationSwap } from "@/domains/swap/infrastructure/station-gateway";

export type { StationSwapResultView };

export async function runStationSwap(input: {
  stationId: string;
  incomingBatteryId: string;
}): Promise<StationSwapResultView> {
  const r = await postStationSwap(input.stationId, input.incomingBatteryId);
  return toStationSwapResultView(r);
}
