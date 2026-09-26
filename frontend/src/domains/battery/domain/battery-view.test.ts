import { describe, expect, it } from "vitest";
import {
  canFinishBatteryCharging,
  canRetireBattery,
  canReturnBatteryForCharging,
  canSwapOutBattery,
  parseBatteryStatus,
  toBatteryView,
} from "./battery-view";

describe("parseBatteryStatus", () => {
  it("accepts contract values", () => {
    expect(parseBatteryStatus("AVAILABLE")).toBe("AVAILABLE");
    expect(parseBatteryStatus("RETIRED")).toBe("RETIRED");
  });

  it("throws on unknown", () => {
    expect(() => parseBatteryStatus("IDLE")).toThrow(/未知电池状态/);
  });
});

describe("battery transitions", () => {
  it("swapOut only AVAILABLE", () => {
    expect(canSwapOutBattery("AVAILABLE")).toBe(true);
    expect(canSwapOutBattery("IN_USE")).toBe(false);
  });

  it("return only IN_USE", () => {
    expect(canReturnBatteryForCharging("IN_USE")).toBe(true);
    expect(canReturnBatteryForCharging("AVAILABLE")).toBe(false);
  });

  it("finish only CHARGING", () => {
    expect(canFinishBatteryCharging("CHARGING")).toBe(true);
    expect(canFinishBatteryCharging("AVAILABLE")).toBe(false);
  });

  it("retire any non-retired", () => {
    expect(canRetireBattery("AVAILABLE")).toBe(true);
    expect(canRetireBattery("RETIRED")).toBe(false);
  });
});

describe("toBatteryView", () => {
  it("AVAILABLE is swappable", () => {
    const view = toBatteryView({ id: "BAT-1", status: "AVAILABLE" });
    expect(view.swapOutAllowed).toBe(true);
    expect(view.statusLabel).toBe("可换出");
    expect(view.blockMessage).toBeNull();
  });

  it("RETIRED is closed", () => {
    const view = toBatteryView({ id: "BAT-1", status: "RETIRED" });
    expect(view.swapOutAllowed).toBe(false);
    expect(view.retireAllowed).toBe(false);
    expect(view.blockMessage).toContain("退役");
  });
});
