import { describe, expect, it } from "vitest";
import {
  canApproveOnboarding,
  onboardingBlockMessage,
  parseOnboardingStatus,
  toOnboardingApplicationView,
} from "./onboarding-application-view";

describe("parseOnboardingStatus", () => {
  it("accepts contract values", () => {
    expect(parseOnboardingStatus("SUBMITTED")).toBe("SUBMITTED");
    expect(parseOnboardingStatus("APPROVED")).toBe("APPROVED");
    expect(parseOnboardingStatus("REJECTED")).toBe("REJECTED");
  });

  it("throws on unknown", () => {
    expect(() => parseOnboardingStatus("PENDING")).toThrow(/未知入驻申请状态/);
  });
});

describe("canApproveOnboarding", () => {
  it("only SUBMITTED", () => {
    expect(canApproveOnboarding("SUBMITTED")).toBe(true);
    expect(canApproveOnboarding("APPROVED")).toBe(false);
    expect(canApproveOnboarding("REJECTED")).toBe(false);
  });
});

describe("toOnboardingApplicationView", () => {
  it("SUBMITTED is approvable", () => {
    const view = toOnboardingApplicationView({
      id: "APP-M1",
      orgId: "ORG-NEW",
      capability: "MERCHANT",
      status: "SUBMITTED",
    });
    expect(view.approveAllowed).toBe(true);
    expect(view.statusLabel).toContain("待审批");
    expect(view.blockMessage).toBeNull();
  });

  it("APPROVED is blocked", () => {
    const view = toOnboardingApplicationView({
      id: "APP-M1",
      orgId: "ORG-NEW",
      capability: "MERCHANT",
      status: "APPROVED",
    });
    expect(view.approveAllowed).toBe(false);
    expect(onboardingBlockMessage(view.status)).toContain("已批准");
  });
});
