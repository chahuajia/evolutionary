import { describe, expect, it } from "vitest";
import {
  canManageOrganizations,
  isOrganizationActive,
  parseOrganizationStatus,
  toOrganizationView,
} from "./organization-view";

describe("parseOrganizationStatus", () => {
  it("accepts contract values", () => {
    expect(parseOrganizationStatus("ACTIVE")).toBe("ACTIVE");
    expect(parseOrganizationStatus("SUSPENDED")).toBe("SUSPENDED");
  });

  it("throws on unknown", () => {
    expect(() => parseOrganizationStatus("DELETED")).toThrow(/未知组织状态/);
  });
});

describe("organization gates", () => {
  it("active only ACTIVE", () => {
    expect(isOrganizationActive("ACTIVE")).toBe(true);
    expect(isOrganizationActive("PENDING")).toBe(false);
  });

  it("canManage requires both ACTIVE", () => {
    expect(canManageOrganizations("ACTIVE", "ACTIVE")).toBe(true);
    expect(canManageOrganizations("ACTIVE", "SUSPENDED")).toBe(false);
    expect(canManageOrganizations("PENDING", "ACTIVE")).toBe(false);
  });
});

describe("toOrganizationView", () => {
  it("ACTIVE can act as manager", () => {
    const view = toOrganizationView({
      id: "ORG-L1",
      name: "华南",
      status: "ACTIVE",
      operatorCapability: true,
    });
    expect(view.active).toBe(true);
    expect(view.canActAsManager).toBe(true);
    expect(view.statusLabel).toBe("有效");
    expect(view.blockMessage).toBeNull();
  });

  it("SUSPENDED is blocked", () => {
    const view = toOrganizationView({
      id: "ORG-X",
      name: "停用",
      status: "SUSPENDED",
    });
    expect(view.canActAsManager).toBe(false);
    expect(view.blockMessage).toContain("停用");
  });
});
