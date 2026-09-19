import { describe, expect, it } from "vitest";
import { formatCentsAsYuan, toAccrualView } from "./accrual-view";

describe("formatCentsAsYuan", () => {
  it("converts cents to yuan string with two decimals", () => {
    expect(formatCentsAsYuan(10000)).toBe("100.00");
  });
});

describe("toAccrualView", () => {
  it("passes through accrual fields and fills amountYuan", () => {
    const view = toAccrualView({
      id: "acc-1",
      orderId: "ord-1",
      orgId: "org-1",
      amountCents: 10000,
      status: "ACCRUED",
    });

    expect(view.id).toBe("acc-1");
    expect(view.orderId).toBe("ord-1");
    expect(view.orgId).toBe("org-1");
    expect(view.amountCents).toBe(10000);
    expect(view.status).toBe("ACCRUED");
    expect(view.amountYuan).toBe("100.00");
  });
});
