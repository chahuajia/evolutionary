import { describe, expect, it } from "vitest";
import { toEffectiveProductView } from "./effective-product-view";

describe("toEffectiveProductView", () => {
  it("fills yuan and override label", () => {
    const view = toEffectiveProductView({
      templateId: "T-1",
      templateVersion: 2,
      overrideId: "OV-1",
      priceCents: 1999,
      displayName: "月卡",
      durationDays: 30,
    });
    expect(view.priceYuan).toBe("19.99");
    expect(view.overrideLabel).toContain("OV-1");
  });

  it("marks missing override", () => {
    const view = toEffectiveProductView({
      templateId: "T-1",
      templateVersion: 1,
      overrideId: null,
      priceCents: 100,
      displayName: "日卡",
      durationDays: 1,
    });
    expect(view.overrideLabel).toBe("无覆盖");
  });
});
