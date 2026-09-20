import { describe, expect, it } from "vitest";
import {
  couponKindLabel,
  toCouponTemplateView,
} from "./coupon-template-view";

describe("toCouponTemplateView", () => {
  it("fills yuan and kind label", () => {
    const view = toCouponTemplateView({
      id: "T-C1",
      faceBudgetCents: 500,
      kind: "FIXED",
      campaignId: "CAMP-OK",
    });
    expect(view.faceYuan).toBe("5.00");
    expect(view.kindLabel).toBe("固定面额");
    expect(view.faceKnown).toBe(true);
  });

  it("marks unknown face", () => {
    const view = toCouponTemplateView({
      id: "T-0",
      faceBudgetCents: 0,
      kind: "",
      campaignId: "C",
    });
    expect(view.faceKnown).toBe(false);
    expect(view.kindLabel).toBe("未标注类型");
  });
});

describe("couponKindLabel", () => {
  it("maps percent", () => {
    expect(couponKindLabel("PERCENT")).toBe("折扣比例");
  });
});
