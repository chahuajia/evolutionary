/**
 * 换电站展示模型 — 视图边界。
 */

export type StationView = {
  readonly id: string;
  readonly name: string;
  readonly canSwapOut: boolean;
  readonly batteryCount: number;
  readonly availabilityLabel: string;
};

export function toStationView(dto: {
  id: string;
  name: string;
  canSwapOut: boolean;
  batteryCount: number;
}): StationView {
  return {
    id: dto.id,
    name: dto.name,
    canSwapOut: dto.canSwapOut,
    batteryCount: dto.batteryCount,
    availabilityLabel: dto.canSwapOut ? "可换出" : "暂不可换",
  };
}
