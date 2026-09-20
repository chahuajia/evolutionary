import { describe, expect, it } from "vitest";
import { toCreditPurchaseView } from "./credit-purchase-view";

describe("toCreditPurchaseView", () => {
  it("builds summary with amount and debt", () => {
    const view = toCreditPurchaseView({
      orderId: "O1",
      entitlementId: "E1",
      productId: "P1",
      paidAmountCents: 1999,
      debtId: "D1",
    });
    expect(view.summary).toContain("订单 O1");
    expect(view.summary).toContain("¥19.99");
    expect(view.summary).toContain("债务 D1");
  });

  it("omits optional fields", () => {
    const view = toCreditPurchaseView({
      orderId: "O2",
      entitlementId: "E2",
    });
    expect(view.productId).toBeNull();
    expect(view.paidAmountCents).toBeNull();
    expect(view.summary).toBe("订单 O2 · 权益 E2");
  });
});
