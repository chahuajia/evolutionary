/**
 * 站内换电结果展示模型 — 视图边界。
 */

export type StationSwapResultView = {
  readonly stationId: string;
  readonly outgoingId: string;
  readonly incomingId: string;
};

export function toStationSwapResultView(dto: {
  stationId: string;
  outgoingId: string;
  incomingId: string;
}): StationSwapResultView {
  return {
    stationId: dto.stationId,
    outgoingId: dto.outgoingId,
    incomingId: dto.incomingId,
  };
}
