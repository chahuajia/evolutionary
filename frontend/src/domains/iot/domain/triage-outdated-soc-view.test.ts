import { describe, expect, it } from "vitest";
import {
  parseTriageNextStep,
  toTriageOutdatedSocView,
  triageNextStepBadgeTone,
} from "./triage-outdated-soc-view";

describe("parseTriageNextStep", () => {
  it("accepts contract values", () => {
    expect(parseTriageNextStep("SHADOW_STALE")).toBe("SHADOW_STALE");
    expect(parseTriageNextStep("CHECK_ADAPTER")).toBe("CHECK_ADAPTER");
  });

  it("throws on unknown", () => {
    expect(() => parseTriageNextStep("COMM_LOST")).toThrow(/未知诊断下一步/);
  });
});

describe("toTriageOutdatedSocView", () => {
  it("maps SHADOW_STALE label and tone", () => {
    const view = toTriageOutdatedSocView({
      batteryId: "B1",
      nextStep: "SHADOW_STALE",
      orderedChecks: ["检查影子"],
      shadow: {
        batteryId: "B1",
        soc: 40,
        voltageMilli: 48000,
        stale: true,
        lastSeenAt: null,
        status: "IDLE",
        lockState: null,
      },
    });
    expect(view.nextStepLabel).toBe("影子已过期");
    expect(view.nextStepBadgeTone).toBe("stale");
    expect(triageNextStepBadgeTone("CHECK_ADAPTER")).toBe("fresh");
    expect(view.shadow.stale).toBe(true);
  });
});
