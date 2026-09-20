import { describe, expect, it } from "vitest";
import {
  canLockCoupon,
  canRedeemCoupon,
  canSelectCouponForCheckout,
  couponBlockMessage,
  parseUserCouponStatus,
  toUserCouponView,
} from "./user-coupon-view";

describe("parseUserCouponStatus", () => {
  it("accepts contract values", () => {
    expect(parseUserCouponStatus("AVAILABLE")).toBe("AVAILABLE");
    expect(parseUserCouponStatus("LOCKED")).toBe("LOCKED");
    expect(parseUserCouponStatus("USED")).toBe("USED");
    expect(parseUserCouponStatus("EXPIRED")).toBe("EXPIRED");
  });

  it("throws on unknown", () => {
    expect(() => parseUserCouponStatus("ACTIVE")).toThrow(/未知用户券状态/);
  });
});

describe("canLockCoupon", () => {
  it("only AVAILABLE", () => {
    expect(canLockCoupon("AVAILABLE")).toBe(true);
    expect(canLockCoupon("LOCKED")).toBe(false);
    expect(canLockCoupon("USED")).toBe(false);
    expect(canLockCoupon("EXPIRED")).toBe(false);
  });
});

describe("canRedeemCoupon", () => {
  it("allows LOCKED or AVAILABLE", () => {
    expect(canRedeemCoupon("AVAILABLE")).toBe(true);
    expect(canRedeemCoupon("LOCKED")).toBe(true);
    expect(canRedeemCoupon("USED")).toBe(false);
    expect(canRedeemCoupon("EXPIRED")).toBe(false);
  });
});

describe("canSelectCouponForCheckout", () => {
  it("only AVAILABLE is selectable", () => {
    expect(canSelectCouponForCheckout("AVAILABLE")).toBe(true);
    expect(canSelectCouponForCheckout("LOCKED")).toBe(false);
  });
});

describe("toUserCouponView", () => {
  it("AVAILABLE: lock/redeem/select yes", () => {
    const view = toUserCouponView({
      id: "C-1",
      userId: "U1",
      templateId: "T-C1",
      status: "AVAILABLE",
    });
    expect(view.lockAllowed).toBe(true);
    expect(view.redeemAllowed).toBe(true);
    expect(view.checkoutSelectable).toBe(true);
    expect(view.statusLabel).toBe("可用");
    expect(view.blockMessage).toBeNull();
  });

  it("USED: all closed", () => {
    const view = toUserCouponView({
      id: "C-1",
      userId: "U1",
      templateId: "T-C1",
      status: "USED",
    });
    expect(view.lockAllowed).toBe(false);
    expect(view.checkoutSelectable).toBe(false);
    expect(couponBlockMessage(view.status)).toContain("已核销");
  });
});
