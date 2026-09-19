import { describe, expect, it } from "vitest";
import { toCheckoutView } from "./mall-checkout-view";

describe("toCheckoutView", () => {
  it("converts paidAmountCents 199 to paidAmountYuan 1.99", () => {
    const view = toCheckoutView({
      orderId: "ord-1",
      userId: "u-1",
      merchantOrgId: "M1",
      status: "PAID",
      paidAmountCents: 199,
      skuId: "S1",
      qty: 1,
      discountCents: 0,
    });

    expect(view.paidAmountYuan).toBe("1.99");
  });

  it("converts discountCents 50 to discountYuan 0.50", () => {
    const view = toCheckoutView({
      orderId: "ord-2",
      userId: "u-1",
      merchantOrgId: "M1",
      status: "PAID",
      paidAmountCents: 150,
      skuId: "S1",
      qty: 1,
      discountCents: 50,
    });

    expect(view.discountYuan).toBe("0.50");
  });

  it("passes through all order fields", () => {
    const view = toCheckoutView({
      orderId: "ord-3",
      userId: "u-2",
      merchantOrgId: "M2",
      status: "PAID",
      paidAmountCents: 199,
      skuId: "S2",
      qty: 3,
      discountCents: 50,
    });

    expect(view.orderId).toBe("ord-3");
    expect(view.userId).toBe("u-2");
    expect(view.merchantOrgId).toBe("M2");
    expect(view.status).toBe("PAID");
    expect(view.paidAmountCents).toBe(199);
    expect(view.paidAmountYuan).toBe("1.99");
    expect(view.skuId).toBe("S2");
    expect(view.qty).toBe(3);
    expect(view.discountCents).toBe(50);
    expect(view.discountYuan).toBe("0.50");
  });
});
