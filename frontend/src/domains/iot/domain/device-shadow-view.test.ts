import { describe, expect, it } from "vitest";
import {
  canDetectCommLost,
  canMeterWithShadow,
  isShadowFresh,
  isShadowInMaintenance,
  parseLockState,
  parseShadowStatus,
  toDeviceShadowView,
} from "./device-shadow-view";

describe("isShadowFresh / canMeterWithShadow", () => {
  it("fresh when not stale", () => {
    expect(isShadowFresh(false)).toBe(true);
    expect(isShadowFresh(true)).toBe(false);
    expect(canMeterWithShadow(false)).toBe(true);
    expect(canMeterWithShadow(true)).toBe(false);
  });
});

describe("canDetectCommLost", () => {
  it("only useful when stale", () => {
    expect(canDetectCommLost(true)).toBe(true);
    expect(canDetectCommLost(false)).toBe(false);
  });
});

describe("parseShadowStatus / parseLockState", () => {
  it("accepts contract values", () => {
    expect(parseShadowStatus("IDLE")).toBe("IDLE");
    expect(parseShadowStatus("MAINTENANCE")).toBe("MAINTENANCE");
    expect(parseLockState("UNLOCKED")).toBe("UNLOCKED");
  });

  it("throws on unknown", () => {
    expect(() => parseShadowStatus("ONLINE")).toThrow(/未知影子业务态/);
    expect(() => parseLockState("HALF")).toThrow(/未知锁态/);
  });
});

describe("toDeviceShadowView", () => {
  it("stale blocks metered swap", () => {
    const view = toDeviceShadowView({
      batteryId: "BAT-1",
      soc: 80,
      voltageMilli: 42000,
      stale: true,
      lastSeenAt: "2026-01-01T00:00:00Z",
    });
    expect(view.fresh).toBe(false);
    expect(view.meteredSwapAllowed).toBe(false);
    expect(view.commLostDetectUseful).toBe(true);
    expect(view.blockMessage).toContain("禁止按电量计费");
    expect(view.status).toBeNull();
  });

  it("fresh allows metered swap and labels status", () => {
    const view = toDeviceShadowView({
      batteryId: "BAT-1",
      soc: 80,
      voltageMilli: 42000,
      stale: false,
      lastSeenAt: "2026-01-01T00:00:00Z",
      status: "IDLE",
      lockState: "UNLOCKED",
    });
    expect(view.meteredSwapAllowed).toBe(true);
    expect(view.blockMessage).toBeNull();
    expect(view.statusLabel).toBe("空闲");
    expect(view.lockStateLabel).toBe("未锁");
    expect(isShadowInMaintenance("MAINTENANCE")).toBe(true);
  });
});
