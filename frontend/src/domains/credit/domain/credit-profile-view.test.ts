import { describe, expect, it } from "vitest";
import { centsToYuan, toCreditProfileView } from "./credit-profile-view";

describe("centsToYuan", () => {
  it("converts cents to yuan number", () => {
    expect(centsToYuan(250)).toBe(2.5);
  });
});

describe("toCreditProfileView", () => {
  it("fills available credit and yuan display fields", () => {
    const view = toCreditProfileView({
      userId: "u-1",
      creditLimit: 10000,
      usedCredit: 2500,
      status: "good",
      scoreTier: "A",
      policyVersion: 1,
    });

    expect(view.availableCreditCents).toBe(7500);
    expect(view.statusLabel).toBe("正常");
    expect(view.availableYuan).toBe("75.00");
    expect(view.usedYuan).toBe("25.00");
    expect(view.limitYuan).toBe("100.00");
  });
});
