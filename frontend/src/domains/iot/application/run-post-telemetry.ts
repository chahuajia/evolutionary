/**
 * 用例：遥测入影（编排 gateway → DeviceShadowView）。
 */

import {
  toDeviceShadowView,
  type DeviceShadowView,
} from "@/domains/iot/domain/device-shadow-view";
import {
  DEFAULT_IOT_BATTERY,
  DEFAULT_TELEMETRY_SOC,
  DEFAULT_TELEMETRY_VENDOR,
  DEFAULT_TELEMETRY_VOLTAGE_MILLI,
  postTelemetry,
  type TelemetryPayload,
} from "@/domains/iot/infrastructure/iot-gateway";

export async function runPostTelemetry(
  batteryId: string = DEFAULT_IOT_BATTERY,
  payload: Partial<TelemetryPayload> = {},
): Promise<DeviceShadowView> {
  const dto = await postTelemetry(batteryId, {
    vendorId: payload.vendorId?.trim() || DEFAULT_TELEMETRY_VENDOR,
    soc: payload.soc ?? DEFAULT_TELEMETRY_SOC,
    voltageMilli: payload.voltageMilli ?? DEFAULT_TELEMETRY_VOLTAGE_MILLI,
  });
  return toDeviceShadowView(dto);
}

export {
  DEFAULT_IOT_BATTERY,
  DEFAULT_TELEMETRY_SOC,
  DEFAULT_TELEMETRY_VENDOR,
  DEFAULT_TELEMETRY_VOLTAGE_MILLI,
};
