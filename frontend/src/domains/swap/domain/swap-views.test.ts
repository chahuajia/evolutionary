import { describe, expect, it } from "vitest";
import { toStationSwapResultView } from "./station-swap-result-view";
import { toSwapLogView } from "./swap-log-view";

describe("toStationSwapResultView", () => {
  it("passes through ids", () => {
    const view = toStationSwapResultView({
      stationId: "S1",
      outgoingId: "B-out",
      incomingId: "B-in",
    });
    expect(view.outgoingId).toBe("B-out");
    expect(view.incomingId).toBe("B-in");
  });
});

describe("toSwapLogView", () => {
  it("passes through log fields", () => {
    const view = toSwapLogView({
      id: "L1",
      stationId: "S1",
      outgoingBatteryId: "B1",
      incomingBatteryId: "B2",
      occurredAt: "2026-01-01T00:00:00Z",
    });
    expect(view.id).toBe("L1");
    expect(view.occurredAt).toContain("2026");
  });
});
