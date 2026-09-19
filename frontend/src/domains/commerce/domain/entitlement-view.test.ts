import { describe, expect, it } from "vitest";
import {
  canFreezeEntitlement,
  canRevokeEntitlement,
  canSwapWithEntitlement,
  canUnfreezeEntitlement,
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
});

describe("toEntitlementView", () => {
  it("ACTIVE is swappable", () => {
    const view = toEntitlementView({ id: "E-1", status: "ACTIVE" });
    expect(view.swapAllowed).toBe(true);
    expect(view.blockMessage).toBeNull();
  });

  it("FROZEN points to repay", () => {
    const view = toEntitlementView({ id: "E-1", status: "FROZEN" });
    expect(view.swapAllowed).toBe(false);
    expect(view.unfreezeAllowed).toBe(true);
    expect(view.blockMessage).toContain("冻结");
  });
});
