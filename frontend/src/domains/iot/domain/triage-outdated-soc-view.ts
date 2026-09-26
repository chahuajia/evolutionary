/**
 * SOC 过时诊断展示模型 — 视图边界。
 *
 * 对齐后端 `TriageOutdatedSoc.NextStep`：仅 SHADOW_STALE | CHECK_ADAPTER。
 */

import {
  toDeviceShadowView,
  type DeviceShadowView,
} from "./device-shadow-view";

export type TriageNextStep = "SHADOW_STALE" | "CHECK_ADAPTER";

const TRIAGE_NEXT_STEPS = ["SHADOW_STALE", "CHECK_ADAPTER"] as const;

export function parseTriageNextStep(raw: unknown): TriageNextStep {
  if (
    typeof raw === "string" &&
    (TRIAGE_NEXT_STEPS as readonly string[]).includes(raw)
  ) {
    return raw as TriageNextStep;
  }
  throw new Error(
    `未知诊断下一步：${String(raw)}（契约：${TRIAGE_NEXT_STEPS.join(" | ")}）`,
  );
}

export const TRIAGE_NEXT_STEP_LABEL: Record<TriageNextStep, string> = {
  SHADOW_STALE: "影子已过期",
  CHECK_ADAPTER: "查适配器",
};

/** 徽章色调：页面只映射 tone→CSS，勿再 nextStep===。 */
export type TriageNextStepBadgeTone = "stale" | "fresh";

export function triageNextStepBadgeTone(
  nextStep: TriageNextStep,
): TriageNextStepBadgeTone {
  if (nextStep === "SHADOW_STALE") return "stale";
  return "fresh";
}

export type TriageOutdatedSocView = {
  readonly batteryId: string;
  readonly nextStep: TriageNextStep;
  readonly nextStepLabel: string;
  readonly nextStepBadgeTone: TriageNextStepBadgeTone;
  readonly orderedChecks: readonly string[];
  readonly shadow: DeviceShadowView;
};

export function toTriageOutdatedSocView(dto: {
  batteryId: string;
  nextStep: string;
  orderedChecks: readonly string[];
  shadow: {
    batteryId: string;
    soc: number;
    voltageMilli: number;
    stale: boolean;
    lastSeenAt: string | null;
    status: string | null;
    lockState: string | null;
  };
}): TriageOutdatedSocView {
  const nextStep = parseTriageNextStep(dto.nextStep);
  return {
    batteryId: dto.batteryId,
    nextStep,
    nextStepLabel: TRIAGE_NEXT_STEP_LABEL[nextStep],
    nextStepBadgeTone: triageNextStepBadgeTone(nextStep),
    orderedChecks: dto.orderedChecks,
    shadow: toDeviceShadowView(dto.shadow),
  };
}
