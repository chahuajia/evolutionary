/**
 * 站详情展示模型 — StationView + BatteryView[] 组合。
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

export type StationDetailView = {
  readonly station: StationView;
  readonly batteries: readonly BatteryView[];
};

export function toStationDetailView(dto: {
  id: string;
  name: string;
  canSwapOut: boolean;
  batteries: readonly { id: string; status: unknown }[];
}): StationDetailView {
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
