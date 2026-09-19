import { describe, expect, it } from "vitest";
import {
  canPayCommerceOrder,
  canRefundCommerceOrder,
  parseCommerceOrderStatus,
  toCommerceOrderView,
} from "./commerce-order-view";

describe("parseCommerceOrderStatus", () => {
  it("accepts contract values", () => {
    expect(parseCommerceOrderStatus("PAID")).toBe("PAID");
    expect(parseCommerceOrderStatus("CREATED")).toBe("CREATED");
  });

  it("throws on unknown", () => {
    expect(() => parseCommerceOrderStatus("SHIPPED")).toThrow(
      /未知换电订单状态/,
    );
  });
});

describe("commerce order gates", () => {
  it("pay only CREATED; refund only PAID", () => {
    expect(canPayCommerceOrder("CREATED")).toBe(true);
    expect(canPayCommerceOrder("PAID")).toBe(false);
    expect(canRefundCommerceOrder("PAID")).toBe(true);
    expect(canRefundCommerceOrder("REFUNDED")).toBe(false);
  });
});

describe("toCommerceOrderView", () => {
  it("PAID is refundable", () => {
    const view = toCommerceOrderView({ orderId: "O-1", status: "PAID" });
    expect(view.refundAllowed).toBe(true);
    expect(view.statusLabel).toBe("已支付");
  });

  it("REFUNDED is blocked", () => {
    const view = toCommerceOrderView({ orderId: "O-1", status: "REFUNDED" });
    expect(view.refundAllowed).toBe(false);
    expect(view.blockMessage).toContain("已退款");
  });
});
