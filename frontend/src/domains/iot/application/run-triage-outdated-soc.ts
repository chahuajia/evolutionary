/**
 * 用例：SOC 过时诊断（编排 gateway → 影子展示模型）。
 */

import {
  toDeviceShadowView,
  type DeviceShadowView,
} from "@/domains/iot/domain/device-shadow-view";
import {
  DEFAULT_IOT_BATTERY,
  postTriageOutdatedSoc,
  type TriageNextStep,
} from "@/domains/iot/infrastructure/iot-gateway";

export type TriageOutdatedSocView = {
  readonly batteryId: string;
  readonly nextStep: TriageNextStep;
  readonly orderedChecks: readonly string[];
  readonly shadow: DeviceShadowView;
};

export async function runTriageOutdatedSoc(
  batteryId: string = DEFAULT_IOT_BATTERY,
): Promise<TriageOutdatedSocView> {
  const r = await postTriageOutdatedSoc(batteryId);
  return {
    batteryId: r.batteryId,
    nextStep: r.nextStep,
    orderedChecks: r.orderedChecks,
    shadow: toDeviceShadowView(r.shadow),
  };
}

export { DEFAULT_IOT_BATTERY };
