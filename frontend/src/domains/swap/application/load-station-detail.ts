/**
 * 用例：加载站详情展示模型（编排 gateway → StationDetailView）。
 */

import {
  toStationDetailView,
  type StationDetailView,
} from "@/domains/swap/domain/station-detail-view";
import { fetchStationDetail } from "@/domains/swap/infrastructure/station-gateway";

export type { StationDetailView };

export async function loadStationDetail(
  stationId: string,
): Promise<StationDetailView> {
  const dto = await fetchStationDetail(stationId);
  return toStationDetailView({
    id: dto.id,
    name: dto.name,
    canSwapOut: Boolean(dto.canSwapOut),
    batteries: dto.batteries,
  });
}
