import { describe, expect, it } from "vitest";
import {
  canCompleteUsageEvent,
  canFailUsageEvent,
  parseUsageEventStatus,
  toUsageEventView,
} from "./usage-event-view";

describe("parseUsageEventStatus", () => {
  it("accepts contract values", () => {
    expect(parseUsageEventStatus("STARTED")).toBe("STARTED");
    expect(parseUsageEventStatus("COMPLETED")).toBe("COMPLETED");
  });

  it("throws on unknown", () => {
    expect(() => parseUsageEventStatus("RUNNING")).toThrow(/未知用量事件状态/);
  });
});

describe("usage event gates", () => {
  it("complete/fail only STARTED", () => {
    expect(canCompleteUsageEvent("STARTED")).toBe(true);
    expect(canFailUsageEvent("STARTED")).toBe(true);
    expect(canCompleteUsageEvent("COMPLETED")).toBe(false);
    expect(canFailUsageEvent("FAILED")).toBe(false);
  });
});

describe("toUsageEventView", () => {
  it("COMPLETED is terminal", () => {
    const view = toUsageEventView({ id: "UE-1", status: "COMPLETED" });
    expect(view.statusLabel).toBe("已完成");
    expect(view.completeAllowed).toBe(false);
    expect(view.blockMessage).toContain("已完成");
  });

  it("STARTED is mutable", () => {
    const view = toUsageEventView({ id: "UE-1", status: "STARTED" });
    expect(view.completeAllowed).toBe(true);
    expect(view.blockMessage).toBeNull();
  });
});
