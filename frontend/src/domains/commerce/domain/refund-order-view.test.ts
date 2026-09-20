import { describe, expect, it } from "vitest";
import { toRefundOrderView } from "./refund-order-view";

describe("toRefundOrderView", () => {
  it("labels entitlement with status", () => {
    const view = toRefundOrderView({
      orderId: "O-1",
      status: "REFUNDED",
      entitlementId: "E-1",
      entitlementStatus: "REVOKED",
    });
    expect(view.order.orderId).toBe("O-1");
    expect(view.entitlementLabel).toContain("E-1");
    expect(view.entitlementLabel).toContain("已撤销");
  });

  it("marks missing entitlement as revoked", () => {
    const view = toRefundOrderView({
      orderId: "O-2",
      status: "REFUNDED",
    });
    expect(view.entitlementId).toBeNull();
    expect(view.entitlementLabel).toBe("(revoked)");
  });
});
