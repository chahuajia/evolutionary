import { describe, expect, it } from "vitest";
import { toStationDetailView } from "./station-detail-view";

describe("toStationDetailView", () => {
  it("builds station and batteries", () => {
    const view = toStationDetailView({
      id: "S-1",
      name: "站A",
      canSwapOut: true,
      batteries: [
        { id: "B-1", status: "AVAILABLE" },
        { id: "B-2", status: "IN_USE" },
      ],
    });
    expect(view.station.batteryCount).toBe(2);
    expect(view.batteries).toHaveLength(2);
    expect(view.batteries[0]?.statusLabel).toBeTruthy();
  });
});
