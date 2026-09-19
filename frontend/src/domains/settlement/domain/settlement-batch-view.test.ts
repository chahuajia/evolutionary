import { describe, expect, it } from "vitest";
import {
  canCloseSettlementBatch,
  parseSettlementBatchStatus,
  toSettlementBatchView,
} from "./settlement-batch-view";

describe("parseSettlementBatchStatus", () => {
  it("accepts OPEN / CLOSED", () => {
    expect(parseSettlementBatchStatus("OPEN")).toBe("OPEN");
    expect(parseSettlementBatchStatus("CLOSED")).toBe("CLOSED");
  });

  it("throws on unknown", () => {
    expect(() => parseSettlementBatchStatus("RUNNING")).toThrow(
      /未知结算批状态/,
    );
  });
});

describe("canCloseSettlementBatch", () => {
  it("only OPEN", () => {
    expect(canCloseSettlementBatch("OPEN")).toBe(true);
    expect(canCloseSettlementBatch("CLOSED")).toBe(false);
  });
});

describe("toSettlementBatchView", () => {
  it("CLOSED from RunSettlementBatch is terminal", () => {
    const view = toSettlementBatchView({
      id: "B-1",
      status: "CLOSED",
      periodStart: "2026-09-01T00:00:00Z",
      periodEnd: "2026-09-08T00:00:00Z",
      closedAt: "2026-09-08T01:00:00Z",
    });
    expect(view.statusLabel).toBe("已关账");
    expect(view.closeAllowed).toBe(false);
    expect(view.blockMessage).toContain("已关账");
  });

  it("OPEN can still close", () => {
    const view = toSettlementBatchView({
      id: "B-2",
      status: "OPEN",
      periodStart: "2026-09-01T00:00:00Z",
      periodEnd: "2026-09-08T00:00:00Z",
      closedAt: null,
    });
    expect(view.closeAllowed).toBe(true);
    expect(view.blockMessage).toBeNull();
  });
});
