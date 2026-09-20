/**
 * 用例：加载站详情展示模型（编排 gateway → StationView + BatteryView[]）。
 */

import {
  parseBatteryStatus,
  toBatteryView,
  type BatteryView,
} from "@/domains/battery/domain/battery-view";
import {
  toStationView,
  type StationView,
} from "@/domains/swap/domain/station-view";
import { fetchStationDetail } from "@/domains/swap/infrastructure/station-gateway";

export type StationDetailView = {
  readonly station: StationView;
  readonly batteries: readonly BatteryView[];
};

export async function loadStationDetail(
  stationId: string,
): Promise<StationDetailView> {
  const dto = await fetchStationDetail(stationId);
  return {
    station: toStationView({
      id: dto.id,
      name: dto.name,
      canSwapOut: Boolean(dto.canSwapOut),
      batteryCount: dto.batteries.length,
    }),
    batteries: dto.batteries.map((b) =>
      toBatteryView({
        id: b.id,
        status: parseBatteryStatus(b.status),
      }),
    ),
  };
}
