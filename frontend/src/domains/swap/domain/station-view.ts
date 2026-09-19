/**
 * 换电站展示模型 — 视图边界。
 */

export type StationView = {
  readonly id: string;
  readonly name: string;
  readonly canSwapOut: boolean;
  readonly batteryCount: number;
  readonly availabilityLabel: string;
  /** 不可换出的站不能被选为本次换电目标 */
  readonly selectable: boolean;
};

/**
 * 展示不变量：不能换出的站不可选为换电目标。
 */
export function canChooseStationForSwap(canSwapOut: boolean): boolean {
  return canSwapOut;
}

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
    availabilityLabel: dto.canSwapOut ? "可换出" : "不可换出",
    selectable: canChooseStationForSwap(dto.canSwapOut),
  };
}
