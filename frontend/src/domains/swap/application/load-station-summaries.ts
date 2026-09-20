/**
 * 用例：加载换电站概览列表（编排 gateway → StationView）。
 */

import {
  toStationView,
  type StationView,
} from "@/domains/swap/domain/station-view";
import { fetchStationSummaries } from "@/domains/swap/infrastructure/station-gateway";

export async function loadStationSummaries(): Promise<readonly StationView[]> {
  const dtos = await fetchStationSummaries();
  return dtos.map(toStationView);
}
