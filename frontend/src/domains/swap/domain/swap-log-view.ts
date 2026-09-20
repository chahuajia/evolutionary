/**
 * 换电日志展示模型 — 视图边界。
 */

export type SwapLogView = {
  readonly id: string;
  readonly stationId: string;
  readonly outgoingBatteryId: string;
  readonly incomingBatteryId: string;
  readonly occurredAt: string;
};

export function toSwapLogView(dto: {
  id: string;
  stationId: string;
  outgoingBatteryId: string;
  incomingBatteryId: string;
  occurredAt: string;
}): SwapLogView {
  return {
    id: dto.id,
    stationId: dto.stationId,
    outgoingBatteryId: dto.outgoingBatteryId,
    incomingBatteryId: dto.incomingBatteryId,
    occurredAt: dto.occurredAt,
  };
}
