/**
 * 用例：SOC 过时诊断（编排 gateway → TriageOutdatedSocView）。
 */

import {
  toTriageOutdatedSocView,
  type TriageOutdatedSocView,
} from "@/domains/iot/domain/triage-outdated-soc-view";
import {
  DEFAULT_IOT_BATTERY,
  postTriageOutdatedSoc,
} from "@/domains/iot/infrastructure/iot-gateway";

export type { TriageOutdatedSocView };

export async function runTriageOutdatedSoc(
  batteryId: string = DEFAULT_IOT_BATTERY,
): Promise<TriageOutdatedSocView> {
  const r = await postTriageOutdatedSoc(batteryId);
  return toTriageOutdatedSocView({
    batteryId: r.batteryId,
    nextStep: r.nextStep,
    orderedChecks: r.orderedChecks,
    shadow: r.shadow,
  });
}

export { DEFAULT_IOT_BATTERY };
