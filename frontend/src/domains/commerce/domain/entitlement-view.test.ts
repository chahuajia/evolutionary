import { describe, expect, it } from "vitest";
import {
  canFreezeEntitlement,
  canRevokeEntitlement,
  canSwapWithEntitlement,
  canUnfreezeEntitlement,
  isExhaustedEntitlement,
  parseEntitlementStatus,
  toEntitlementView,
} from "./entitlement-view";

describe("parseEntitlementStatus", () => {
  it("accepts contract values", () => {
    expect(parseEntitlementStatus("ACTIVE")).toBe("ACTIVE");
    expect(parseEntitlementStatus("FROZEN")).toBe("FROZEN");
  });

  it("throws on unknown", () => {
    expect(() => parseEntitlementStatus("PENDING")).toThrow(/未知权益状态/);
  });
});

describe("entitlement gates", () => {
  it("swap/freeze only ACTIVE", () => {
    expect(canSwapWithEntitlement("ACTIVE")).toBe(true);
    expect(canSwapWithEntitlement("FROZEN")).toBe(false);
    expect(canFreezeEntitlement("ACTIVE")).toBe(true);
    expect(canFreezeEntitlement("FROZEN")).toBe(false);
  });

  it("unfreeze only FROZEN", () => {
    expect(canUnfreezeEntitlement("FROZEN")).toBe(true);
    expect(canUnfreezeEntitlement("ACTIVE")).toBe(false);
  });

  it("revoke excludes REVOKED", () => {
    expect(canRevokeEntitlement("ACTIVE")).toBe(true);
    expect(canRevokeEntitlement("REVOKED")).toBe(false);
  });

  it("exhausted when remaining is 0", () => {
    expect(isExhaustedEntitlement(0)).toBe(true);
    expect(isExhaustedEntitlement(1)).toBe(false);
    expect(isExhaustedEntitlement(null)).toBe(false);
  });
});

describe("toEntitlementView", () => {
  it("ACTIVE unlimited is swappable", () => {
    const view = toEntitlementView({ id: "E-1", status: "ACTIVE" });
    expect(view.swapAllowed).toBe(true);
    expect(view.remainingSwaps).toBeNull();
    expect(view.blockMessage).toBeNull();
  });

  it("ACTIVE with remaining > 0 is swappable", () => {
    const view = toEntitlementView({
      id: "E-1",
      status: "ACTIVE",
      remainingSwaps: 2,
    });
    expect(view.swapAllowed).toBe(true);
    expect(view.blockMessage).toBeNull();
  });

  it("ACTIVE exhausted is not swappable", () => {
    const view = toEntitlementView({
      id: "E-1",
      status: "ACTIVE",
      remainingSwaps: 0,
    });
    expect(view.swapAllowed).toBe(false);
    expect(view.blockMessage).toContain("用尽");
  });

  it("FROZEN points to repay", () => {
    const view = toEntitlementView({ id: "E-1", status: "FROZEN" });
    expect(view.swapAllowed).toBe(false);
    expect(view.unfreezeAllowed).toBe(true);
    expect(view.blockMessage).toContain("冻结");
  });
});
