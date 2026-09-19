import { describe, expect, it } from "vitest";
import {
  accrualBlockMessage,
  canReverseAccrual,
  canSettleAccrual,
  formatCentsAsYuan,
  parseAccrualStatus,
  toAccrualView,
} from "./accrual-view";

// ── 边界解析（parse-dont-validate）────────────────────────
// 网关原先写 `String(r.status ?? "")`：未知状态静默变空串往下传。
// 正确的边界行为是**当场炸**。

describe("parseAccrualStatus", () => {
  it("accepts the three contract values", () => {
    expect(parseAccrualStatus("PENDING")).toBe("PENDING");
    expect(parseAccrualStatus("SETTLED")).toBe("SETTLED");
    expect(parseAccrualStatus("REVERSED")).toBe("REVERSED");
  });

  it("throws on the drifted value that used to pass silently", () => {
    expect(() => parseAccrualStatus("ACCRUED")).toThrow(/未知应计状态/);
  });

  it("throws on missing / non-string", () => {
    expect(() => parseAccrualStatus(undefined)).toThrow();
    expect(() => parseAccrualStatus("")).toThrow();
    expect(() => parseAccrualStatus(42)).toThrow();
  });
});

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
      // 后端 Status = PENDING | SETTLED | REVERSED。
      // 此处原本写 "ACCRUED" —— 那个状态**后端从来不存在**（契约漂移，见 S33）。
      status: "PENDING",
    });

    expect(view.id).toBe("acc-1");
    expect(view.orderId).toBe("ord-1");
    expect(view.orgId).toBe("org-1");
    expect(view.amountCents).toBe(10000);
    expect(view.status).toBe("PENDING");
    expect(view.amountYuan).toBe("100.00");
  });
});

// ── 展示不变量（对齐后端守卫）────────────────────────────
// `ProfitShareAccrual.settle` 仅 PENDING 可结算；
// `reverse` 仅 PENDING 可冲销（否则 ORDER_NOT_REFUNDABLE_SETTLED「已结算分润不可退款」）。
// 两条守卫判据相同：**非 PENDING 一律不可动**。

describe("canSettleAccrual", () => {
  it("only PENDING can be settled", () => {
    expect(canSettleAccrual("PENDING")).toBe(true);
    expect(canSettleAccrual("SETTLED")).toBe(false);
    expect(canSettleAccrual("REVERSED")).toBe(false);
  });
});

describe("canReverseAccrual", () => {
  it("only PENDING can be reversed", () => {
    expect(canReverseAccrual("PENDING")).toBe(true);
    expect(canReverseAccrual("SETTLED")).toBe(false);
    expect(canReverseAccrual("REVERSED")).toBe(false);
  });
});

describe("accrualBlockMessage", () => {
  it("is null when actionable", () => {
    expect(accrualBlockMessage("PENDING")).toBeNull();
  });

  it("explains SETTLED as terminal", () => {
    const msg = accrualBlockMessage("SETTLED");
    expect(msg).not.toBeNull();
    expect(msg).toContain("已结算");
  });

  it("distinguishes REVERSED from SETTLED", () => {
    expect(accrualBlockMessage("REVERSED")).not.toBe(accrualBlockMessage("SETTLED"));
  });
});

describe("toAccrualView 的展示门", () => {
  it("PENDING → settle/reverse 均可，无拦阻说明", () => {
    const view = toAccrualView({
      id: "a",
      orderId: "o",
      orgId: "g",
      amountCents: 1,
      status: "PENDING",
    });
    expect(view.settleAllowed).toBe(true);
    expect(view.reverseAllowed).toBe(true);
    expect(view.blockMessage).toBeNull();
  });

  it("SETTLED → 两个都不可，且给出原因", () => {
    const view = toAccrualView({
      id: "a",
      orderId: "o",
      orgId: "g",
      amountCents: 1,
      status: "SETTLED",
    });
    expect(view.settleAllowed).toBe(false);
    expect(view.reverseAllowed).toBe(false);
    expect(view.blockMessage).toContain("已结算");
  });
});
