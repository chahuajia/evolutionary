import { describe, expect, it } from "vitest";
import { canChooseStationForSwap, toStationView } from "./station-view";

describe("canChooseStationForSwap", () => {
  it("allows choosing a station that can swap out", () => {
    expect(canChooseStationForSwap(true)).toBe(true);
  });

  it("forbids choosing a station that cannot swap out", () => {
    expect(canChooseStationForSwap(false)).toBe(false);
  });
});

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
    expect(view.selectable).toBe(true);
    expect(view.blockMessage).toBeNull();
  });

  it("passes through station fields and sets availabilityLabel to 不可换出 when canSwapOut is false", () => {
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
    expect(view.availabilityLabel).toBe("不可换出");
    expect(view.selectable).toBe(false);
    expect(view.blockMessage).toContain("不可换出");
  });
});
