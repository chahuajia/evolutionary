import { describe, expect, it } from "vitest";
import {
  canActivateOverride,
  canRevokeOverride,
  isOverrideRevoked,
  overrideBlockMessage,
  parsePackageOverrideStatus,
  toPackageOverrideView,
} from "./package-override-view";

// 对齐后端 PackageOverride：activate 仅 DRAFT；revoke 仅 ACTIVE

describe("parsePackageOverrideStatus", () => {
  it("accepts contract values", () => {
    expect(parsePackageOverrideStatus("DRAFT")).toBe("DRAFT");
    expect(parsePackageOverrideStatus("ACTIVE")).toBe("ACTIVE");
    expect(parsePackageOverrideStatus("REVOKED")).toBe("REVOKED");
  });

  it("throws on unknown", () => {
    expect(() => parsePackageOverrideStatus("PUBLISHED")).toThrow(
      /未知套餐覆盖状态/,
    );
  });
});

describe("canActivateOverride", () => {
  it("only DRAFT can activate", () => {
    expect(canActivateOverride("DRAFT")).toBe(true);
    expect(canActivateOverride("ACTIVE")).toBe(false);
    expect(canActivateOverride("REVOKED")).toBe(false);
  });
});

describe("canRevokeOverride", () => {
  it("only ACTIVE can revoke", () => {
    expect(canRevokeOverride("ACTIVE")).toBe(true);
    expect(canRevokeOverride("DRAFT")).toBe(false);
    expect(canRevokeOverride("REVOKED")).toBe(false);
  });
});

describe("overrideBlockMessage", () => {
  it("is null for DRAFT", () => {
    expect(overrideBlockMessage("DRAFT")).toBeNull();
  });

  it("points ACTIVE to revoke path", () => {
    expect(overrideBlockMessage("ACTIVE")).toContain("撤销");
  });

  it("marks REVOKED as terminal", () => {
    expect(overrideBlockMessage("REVOKED")).toContain("已撤销");
  });
});

describe("toPackageOverrideView", () => {
  it("ACTIVE: revoke yes, activate no", () => {
    const view = toPackageOverrideView({
      overrideId: "OV-1",
      orgId: "ORG-L2",
      templateId: "T-PUB-1",
      templateVersion: 1,
      status: "ACTIVE",
      priceCents: 1999,
    });
    expect(view.activateAllowed).toBe(false);
    expect(view.revokeAllowed).toBe(true);
    expect(view.revoked).toBe(false);
    expect(view.statusLabel).toBe("已激活");
    expect(view.blockMessage).toContain("撤销");
    expect(view.priceYuan).toBe("19.99");
  });

  it("REVOKED: both closed", () => {
    const view = toPackageOverrideView({
      overrideId: "OV-1",
      orgId: "ORG-L2",
      templateId: "T-PUB-1",
      templateVersion: 1,
      status: "REVOKED",
    });
    expect(view.activateAllowed).toBe(false);
    expect(view.revokeAllowed).toBe(false);
    expect(view.revoked).toBe(true);
    expect(isOverrideRevoked("REVOKED")).toBe(true);
    expect(view.statusLabel).toBe("已撤销");
    expect(view.priceYuan).toBeNull();
  });
});
