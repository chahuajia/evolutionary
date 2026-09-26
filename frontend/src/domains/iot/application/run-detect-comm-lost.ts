/**
 * 用例：通信丢失检测（编排 gateway → DetectCommLostView）。
 */

import {
  toDetectCommLostView,
  type DetectCommLostView,
} from "@/domains/iot/domain/detect-comm-lost-view";
import {
  DEFAULT_IOT_BATTERY,
  postDetectCommLost,
} from "@/domains/iot/infrastructure/iot-gateway";

export type { DetectCommLostView };

export async function runDetectCommLost(
  batteryId: string = DEFAULT_IOT_BATTERY,
): Promise<DetectCommLostView> {
  const r = await postDetectCommLost(batteryId);
  return toDetectCommLostView(r);
}

export { DEFAULT_IOT_BATTERY };
