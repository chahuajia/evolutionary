/**
 * 用例：加载设备影子展示模型（编排 gateway → DeviceShadowView）。
 */

import {
  toDeviceShadowView,
  type DeviceShadowView,
} from "@/domains/iot/domain/device-shadow-view";
import {
  DEFAULT_IOT_BATTERY,
  fetchDeviceShadow,
} from "@/domains/iot/infrastructure/iot-gateway";

export async function loadDeviceShadow(
  batteryId: string = DEFAULT_IOT_BATTERY,
): Promise<DeviceShadowView> {
  const dto = await fetchDeviceShadow(batteryId);
  return toDeviceShadowView(dto);
}

export { DEFAULT_IOT_BATTERY };
