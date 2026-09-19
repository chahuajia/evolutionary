import { describe, expect, it } from "vitest";
import {
  canTradeWithMerchant,
  merchantTradeBlockMessage,
  parseMerchantProfileStatus,
  toMerchantProfileView,
} from "./merchant-profile-view";

describe("parseMerchantProfileStatus", () => {
  it("accepts ACTIVE / SUSPENDED", () => {
    expect(parseMerchantProfileStatus("ACTIVE")).toBe("ACTIVE");
    expect(parseMerchantProfileStatus("SUSPENDED")).toBe("SUSPENDED");
  });

  it("throws on unknown", () => {
    expect(() => parseMerchantProfileStatus("DRAFT")).toThrow(
      /未知商家档案状态/,
    );
  });
});

describe("canTradeWithMerchant", () => {
  it("only ACTIVE", () => {
    expect(canTradeWithMerchant("ACTIVE")).toBe(true);
    expect(canTradeWithMerchant("SUSPENDED")).toBe(false);
  });
});

describe("toMerchantProfileView", () => {
  it("ACTIVE is tradeable", () => {
    const view = toMerchantProfileView({
      orgId: "M1",
      shopName: "店",
      status: "ACTIVE",
    });
    expect(view.tradeAllowed).toBe(true);
    expect(view.statusLabel).toBe("营业中");
    expect(view.blockMessage).toBeNull();
  });

  it("SUSPENDED is blocked", () => {
    const view = toMerchantProfileView({
      orgId: "M1",
      shopName: "店",
      status: "SUSPENDED",
    });
    expect(view.tradeAllowed).toBe(false);
    expect(merchantTradeBlockMessage(view.status)).toContain("停用");
  });
});
