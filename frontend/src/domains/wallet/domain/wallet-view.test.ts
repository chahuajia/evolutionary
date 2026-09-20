import { describe, expect, it } from "vitest";
import {
  canCoverCents,
  formatCentsAsYuan,
  hasSpendableBalance,
  toWalletView,
  walletCoverGate,
} from "./wallet-view";

describe("formatCentsAsYuan", () => {
  it("converts cents to yuan string with two decimals", () => {
    expect(formatCentsAsYuan(250)).toBe("2.50");
  });
});

describe("canCoverCents", () => {
  it("aligns Account.canCoverCents", () => {
    expect(canCoverCents(250, 250)).toBe(true);
    expect(canCoverCents(250, 251)).toBe(false);
    expect(canCoverCents(0, 0)).toBe(true);
    expect(canCoverCents(10, -1)).toBe(false);
  });
});

describe("hasSpendableBalance", () => {
  it("requires positive cents", () => {
    expect(hasSpendableBalance(1)).toBe(true);
    expect(hasSpendableBalance(0)).toBe(false);
  });
});

describe("walletCoverGate", () => {
  it("allows when balance covers amount", () => {
    const wallet = toWalletView({
      userId: "u-1",
      balanceCents: 500,
      pointsCents: 0,
      currency: "CNY",
    });
    expect(walletCoverGate(wallet, 250)).toEqual({
      coverAllowed: true,
      blockMessage: null,
    });
  });

  it("blocks with unified shortfall message", () => {
    const wallet = toWalletView({
      userId: "u-1",
      balanceCents: 100,
      pointsCents: 0,
      currency: "CNY",
    });
    const gate = walletCoverGate(wallet, 250);
    expect(gate.coverAllowed).toBe(false);
    expect(gate.blockMessage).toContain("余额不足");
    expect(gate.blockMessage).toContain("1.00");
    expect(gate.blockMessage).toContain("2.50");
  });
});

describe("toWalletView", () => {
  it("fills yuan and spendable gates", () => {
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
    expect(view.hasSpendableBalance).toBe(true);
    expect(view.hasSpendablePoints).toBe(true);
  });

  it("marks empty wallet as not spendable", () => {
    const view = toWalletView({
      userId: "u-2",
      balanceCents: 0,
      pointsCents: 0,
      currency: "CNY",
    });
    expect(view.hasSpendableBalance).toBe(false);
    expect(view.hasSpendablePoints).toBe(false);
  });
});
