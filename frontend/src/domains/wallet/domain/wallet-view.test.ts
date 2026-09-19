import { describe, expect, it } from "vitest";
import { formatCentsAsYuan, toWalletView } from "./wallet-view";

describe("formatCentsAsYuan", () => {
  it("converts cents to yuan string with two decimals", () => {
    expect(formatCentsAsYuan(250)).toBe("2.50");
  });
});

describe("toWalletView", () => {
  it("fills balanceYuan and pointsYuan from cents", () => {
    const view = toWalletView({
      userId: "u-1",
      balanceCents: 250,
      pointsCents: 100,
      currency: "CNY",
    });

    expect(view.balanceYuan).toBe("2.50");
    expect(view.pointsYuan).toBe("1.00");
    expect(view.userId).toBe("u-1");
    expect(view.currency).toBe("CNY");
  });
});
