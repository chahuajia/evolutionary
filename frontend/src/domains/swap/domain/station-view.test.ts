import { describe, expect, it } from "vitest";
import { toStationView } from "./station-view";

describe("toStationView", () => {
  it("passes through station fields and sets availabilityLabel to 可换出 when canSwapOut is true", () => {
    const view = toStationView({
      id: "st-1",
      name: "一号站",
      canSwapOut: true,
      batteryCount: 5,
    });

    expect(view.id).toBe("st-1");
    expect(view.name).toBe("一号站");
    expect(view.canSwapOut).toBe(true);
    expect(view.batteryCount).toBe(5);
    expect(view.availabilityLabel).toBe("可换出");
  });

  it("passes through station fields and sets availabilityLabel to 暂不可换 when canSwapOut is false", () => {
    const view = toStationView({
      id: "st-2",
      name: "二号站",
      canSwapOut: false,
      batteryCount: 0,
    });

    expect(view.id).toBe("st-2");
    expect(view.name).toBe("二号站");
    expect(view.canSwapOut).toBe(false);
    expect(view.batteryCount).toBe(0);
    expect(view.availabilityLabel).toBe("暂不可换");
  });
});
