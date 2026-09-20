import { describe, expect, it } from "vitest";
import {
  canClaimCoupon,
  canClaimFromCampaign,
  campaignClaimBlockMessage,
  hasCampaignBudget,
  parseCampaignStatus,
  toCampaignView,
} from "./campaign-view";

describe("parseCampaignStatus", () => {
  it("accepts contract values", () => {
    expect(parseCampaignStatus("ACTIVE")).toBe("ACTIVE");
    expect(parseCampaignStatus("DRAFT")).toBe("DRAFT");
    expect(parseCampaignStatus("ENDED")).toBe("ENDED");
  });

  it("throws on unknown", () => {
    expect(() => parseCampaignStatus("RUNNING")).toThrow(/未知活动状态/);
  });
});

describe("canClaimFromCampaign", () => {
  it("only ACTIVE", () => {
    expect(canClaimFromCampaign("ACTIVE")).toBe(true);
    expect(canClaimFromCampaign("DRAFT")).toBe(false);
    expect(canClaimFromCampaign("ENDED")).toBe(false);
  });
});

describe("hasCampaignBudget", () => {
  it("requires remaining >= face", () => {
    expect(hasCampaignBudget(100, 100)).toBe(true);
    expect(hasCampaignBudget(99, 100)).toBe(false);
  });
});

describe("canClaimCoupon", () => {
  it("combines status and budget", () => {
    expect(canClaimCoupon("ACTIVE", 50, 10)).toBe(true);
    expect(canClaimCoupon("ACTIVE", 5, 10)).toBe(false);
    expect(canClaimCoupon("ENDED", 100, 10)).toBe(false);
  });
});

describe("toCampaignView", () => {
  it("ACTIVE with budget is claimable", () => {
    const view = toCampaignView({
      id: "CAMP-OK",
      ownerOrgId: "M1",
      name: "ok",
      budgetRemainingCents: 1000,
      status: "ACTIVE",
    });
    expect(view.claimAllowed).toBe(true);
    expect(view.statusLabel).toBe("进行中");
    expect(view.blockMessage).toBeNull();
  });

  it("ENDED is blocked", () => {
    const view = toCampaignView({
      id: "CAMP-X",
      ownerOrgId: "M1",
      name: "x",
      budgetRemainingCents: 1000,
      status: "ENDED",
    });
    expect(view.claimAllowed).toBe(false);
    expect(campaignClaimBlockMessage(view.status, view.budgetRemainingCents)).toContain(
      "已结束",
    );
  });
});
