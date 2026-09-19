import { describe, expect, it } from "vitest";
import {
  canMarkStatementOverdue,
  canRepayStatement,
  parseBillingStatementStatus,
  toBillingStatementView,
} from "./billing-statement-view";

describe("parseBillingStatementStatus", () => {
  it("accepts contract values", () => {
    expect(parseBillingStatementStatus("DUE")).toBe("DUE");
    expect(parseBillingStatementStatus("OVERDUE")).toBe("OVERDUE");
  });

  it("throws on unknown", () => {
    expect(() => parseBillingStatementStatus("PENDING")).toThrow(
      /未知账单状态/,
    );
  });
});

describe("billing statement gates", () => {
  it("repay only DUE/OVERDUE", () => {
    expect(canRepayStatement("DUE")).toBe(true);
    expect(canRepayStatement("OVERDUE")).toBe(true);
    expect(canRepayStatement("PAID")).toBe(false);
    expect(canRepayStatement("OPEN")).toBe(false);
  });

  it("mark overdue only DUE", () => {
    expect(canMarkStatementOverdue("DUE")).toBe(true);
    expect(canMarkStatementOverdue("OVERDUE")).toBe(false);
    expect(canMarkStatementOverdue("PAID")).toBe(false);
  });
});

describe("toBillingStatementView", () => {
  it("DUE is repayable", () => {
    const view = toBillingStatementView({
      id: "STMT-1",
      userId: "U1",
      status: "DUE",
      totalDue: 3000,
      periodStart: "2026-02-01",
      periodEnd: "2026-02-28",
      dueDate: "2026-03-07",
    });
    expect(view.repayAllowed).toBe(true);
    expect(view.markOverdueAllowed).toBe(true);
    expect(view.statusLabel).toBe("待还");
    expect(view.blockMessage).toBeNull();
  });

  it("PAID is blocked", () => {
    const view = toBillingStatementView({
      id: "STMT-1",
      userId: "U1",
      status: "PAID",
      totalDue: 3000,
      periodStart: "2026-02-01",
      periodEnd: "2026-02-28",
      dueDate: "2026-03-07",
      paidAt: "2026-03-01T00:00:00Z",
    });
    expect(view.repayAllowed).toBe(false);
    expect(view.blockMessage).toContain("已还清");
  });
});
