import { describe, expect, it } from "vitest";
import {
  canMarkMallOrderPaid,
  forbidsEntitlement,
  formatCentsAsYuan,
  parseMallOrderStatus,
  toMallOrderView,
} from "./mall-order-view";

describe("formatCentsAsYuan", () => {
  it("converts cents to yuan string with two decimals", () => {
    expect(formatCentsAsYuan(199)).toBe("1.99");
  });
});

describe("parseMallOrderStatus", () => {
  it("accepts contract values", () => {
    expect(parseMallOrderStatus("CREATED")).toBe("CREATED");
    expect(parseMallOrderStatus("PAID")).toBe("PAID");
  });

  it("throws on unknown", () => {
    expect(() => parseMallOrderStatus("PENDING")).toThrow(/未知商城订单状态/);
  });
});

describe("canMarkMallOrderPaid", () => {
  it("only CREATED", () => {
    expect(canMarkMallOrderPaid("CREATED")).toBe(true);
    expect(canMarkMallOrderPaid("PAID")).toBe(false);
    expect(canMarkMallOrderPaid("REFUNDED")).toBe(false);
  });
});

describe("forbidsEntitlement", () => {
  it("is true after CREATED (INV-16)", () => {
    expect(forbidsEntitlement("CREATED")).toBe(false);
    expect(forbidsEntitlement("PAID")).toBe(true);
  });
});

describe("toMallOrderView", () => {
  it("fills yuan and gates for PAID", () => {
    const view = toMallOrderView({
      orderId: "ord-1",
      userId: "u-1",
      merchantOrgId: "M1",
      status: "PAID",
      paidAmountCents: 199,
      skuId: "S1",
      qty: 2,
    });

    expect(view.orderId).toBe("ord-1");
    expect(view.status).toBe("PAID");
    expect(view.statusLabel).toBe("已支付");
    expect(view.paidAmountYuan).toBe("1.99");
    expect(view.payAllowed).toBe(false);
    expect(view.entitlementForbidden).toBe(true);
    expect(view.blockMessage).toContain("INV-16");
  });

  it("CREATED still allows pay", () => {
    const view = toMallOrderView({
      orderId: "ord-2",
      userId: "u-1",
      merchantOrgId: "M1",
      status: "CREATED",
      paidAmountCents: 100,
      skuId: "S1",
      qty: 1,
    });
    expect(view.payAllowed).toBe(true);
    expect(view.entitlementForbidden).toBe(false);
    expect(view.blockMessage).toBeNull();
  });
});
