import { describe, expect, it } from "vitest";
import { formatCentsAsYuan, toMallOrderView } from "./mall-order-view";

describe("formatCentsAsYuan", () => {
  it("converts cents to yuan string with two decimals", () => {
    expect(formatCentsAsYuan(199)).toBe("1.99");
  });
});

describe("toMallOrderView", () => {
  it("passes through order fields and fills paidAmountYuan", () => {
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
    expect(view.userId).toBe("u-1");
    expect(view.merchantOrgId).toBe("M1");
    expect(view.status).toBe("PAID");
    expect(view.paidAmountCents).toBe(199);
    expect(view.paidAmountYuan).toBe("1.99");
    expect(view.skuId).toBe("S1");
    expect(view.qty).toBe(2);
  });
});
