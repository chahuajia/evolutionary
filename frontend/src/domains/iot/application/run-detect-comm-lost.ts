/**
 * 用例：通信丢失检测（编排 gateway → 展示模型）。
 */

import { canDetectCommLost } from "@/domains/iot/domain/device-shadow-view";
import {
  DEFAULT_IOT_BATTERY,
  postDetectCommLost,
} from "@/domains/iot/infrastructure/iot-gateway";

export type DetectCommLostView = {
  readonly batteryId: string;
  readonly stale: boolean;
  readonly raised: boolean;
  /** 如 COMM_LOST；未触发时为 null */
  readonly alertType: string | null;
  /** 运维工单 id；无单时为 null */
  readonly ticketId: string | null;
  /** 本次结果上的 stale 是否仍值得检测（对齐 canDetectCommLost）。 */
  readonly detectUseful: boolean;
};

export async function runDetectCommLost(
  batteryId: string = DEFAULT_IOT_BATTERY,
): Promise<DetectCommLostView> {
  const r = await postDetectCommLost(batteryId);
  return {
    batteryId: r.batteryId,
    stale: r.stale,
    raised: r.raised,
    alertType: r.alertType,
    ticketId: r.ticketId,
    detectUseful: canDetectCommLost(r.stale),
  };
}

export { DEFAULT_IOT_BATTERY };
