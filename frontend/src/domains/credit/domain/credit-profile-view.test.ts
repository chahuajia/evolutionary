import { describe, expect, it } from "vitest";
import {
  availableCreditCents,
  canPurchaseOnCredit,
  centsToYuan,
  canOfferCreditRepay,
  creditPurchaseBlock,
  creditPurchaseBlockMessage,
  hasCreditHeadroom,
  toCreditProfileView,
} from "./credit-profile-view";

describe("availableCreditCents", () => {
  it("is limit minus used", () => {
    expect(availableCreditCents(10000, 2500)).toBe(7500);
  });

  it("goes negative when used exceeds limit", () => {
    expect(availableCreditCents(10000, 12000)).toBe(-2000);
  });
});

describe("canPurchaseOnCredit", () => {
  it("allows only good", () => {
    expect(canPurchaseOnCredit("good")).toBe(true);
    expect(canPurchaseOnCredit("frozen")).toBe(false);
    expect(canPurchaseOnCredit("overdue")).toBe(false);
  });
});

describe("hasCreditHeadroom", () => {
  it("requires positive available cents", () => {
    expect(hasCreditHeadroom(1)).toBe(true);
    expect(hasCreditHeadroom(0)).toBe(false);
    expect(hasCreditHeadroom(-2000)).toBe(false);
  });
});

describe("creditPurchaseBlock", () => {
  it("reports status before limit", () => {
    expect(creditPurchaseBlock("frozen", -100)).toBe("status");
    expect(creditPurchaseBlock("good", 0)).toBe("limit");
    expect(creditPurchaseBlock("good", 1)).toBe("ok");
  });
});

describe("canOfferCreditRepay", () => {
  it("offers repay when used or not good", () => {
    expect(canOfferCreditRepay("good", 0)).toBe(false);
    expect(canOfferCreditRepay("good", 1)).toBe(true);
    expect(canOfferCreditRepay("frozen", 0)).toBe(true);
    expect(canOfferCreditRepay("overdue", 0)).toBe(true);
  });
});

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
    expect(view.purchaseAllowed).toBe(true);
    expect(view.purchaseBlock).toBe("ok");
    expect(view.repayAllowed).toBe(true);
  });

  it("blocks purchase when frozen", () => {
    const view = toCreditProfileView({
      userId: "u-1",
      creditLimit: 10000,
      usedCredit: 2500,
      status: "frozen",
      scoreTier: "C",
      policyVersion: 1,
    });
    expect(view.purchaseAllowed).toBe(false);
    expect(view.purchaseBlock).toBe("status");
    expect(view.statusLabel).toBe("冻结");
  });

  it("blocks purchase when available is zero", () => {
    const view = toCreditProfileView({
      userId: "u-1",
      creditLimit: 10000,
      usedCredit: 10000,
      status: "good",
      scoreTier: "B",
      policyVersion: 1,
    });
    expect(view.purchaseAllowed).toBe(false);
    expect(view.purchaseBlock).toBe("limit");
    expect(creditPurchaseBlockMessage(view.purchaseBlock, view.statusLabel, view.availableYuan)).toContain(
      "可用额度不足",
    );
    expect(view.repayAllowed).toBe(true);
  });

  it("hides repay when good and unused", () => {
    const view = toCreditProfileView({
      userId: "u-1",
      creditLimit: 10000,
      usedCredit: 0,
      status: "good",
      scoreTier: "A",
      policyVersion: 1,
    });
    expect(view.repayAllowed).toBe(false);
  });
});
