import { describe, expect, it } from "vitest";
import {
  defaultSelectBlockMessage,
  isExhaustedEntitlement,
  isFiniteEntitlement,
  selectDefaultEntitlement,
  toSelectableEntitlement,
  type SelectableEntitlement,
} from "./select-entitlement";

describe("isFiniteEntitlement", () => {
  it("null remaining = unlimited", () => {
    expect(isFiniteEntitlement(null)).toBe(false);
    expect(isFiniteEntitlement(5)).toBe(true);
  });
});

describe("isExhaustedEntitlement", () => {
  it("only finite zero is exhausted", () => {
    expect(isExhaustedEntitlement(null)).toBe(false);
    expect(isExhaustedEntitlement(1)).toBe(false);
    expect(isExhaustedEntitlement(0)).toBe(true);
  });
});

describe("selectDefaultEntitlement", () => {
  const unlimited: SelectableEntitlement = {
    id: "E-1",
    status: "ACTIVE",
    remainingSwaps: null,
  };
  const finite: SelectableEntitlement = {
    id: "E-FINITE",
    status: "ACTIVE",
    remainingSwaps: 5,
  };
  const exhausted: SelectableEntitlement = {
    id: "E-DONE",
    status: "ACTIVE",
    remainingSwaps: 0,
  };
  const frozen: SelectableEntitlement = {
    id: "E-FROZEN",
    status: "FROZEN",
    remainingSwaps: 3,
  };

  it("prefers FINITE over UNLIMITED (AC-14)", () => {
    expect(selectDefaultEntitlement([unlimited, finite])?.id).toBe("E-FINITE");
    expect(selectDefaultEntitlement([finite, unlimited])?.id).toBe("E-FINITE");
  });

  it("falls back to UNLIMITED when no usable FINITE", () => {
    expect(selectDefaultEntitlement([unlimited, exhausted])?.id).toBe("E-1");
  });

  it("skips FROZEN and exhausted", () => {
    expect(selectDefaultEntitlement([frozen, exhausted])).toBeNull();
    expect(selectDefaultEntitlement([frozen, unlimited])?.id).toBe("E-1");
  });

  it("returns null when empty", () => {
    expect(selectDefaultEntitlement([])).toBeNull();
  });
});

describe("defaultSelectBlockMessage", () => {
  it("explains empty usable catalog without raw ACTIVE", () => {
    const msg = defaultSelectBlockMessage([
      { id: "E-DONE", status: "ACTIVE", remainingSwaps: 0 },
      { id: "E-F", status: "FROZEN", remainingSwaps: 1 },
    ]);
    expect(msg).toContain("无可用权益");
    expect(msg).not.toContain("ACTIVE");
  });

  it("is null when a default exists", () => {
    expect(
      defaultSelectBlockMessage([
        { id: "E-1", status: "ACTIVE", remainingSwaps: null },
      ]),
    ).toBeNull();
  });
});

describe("toSelectableEntitlement", () => {
  it("parses status from wire", () => {
    const row = toSelectableEntitlement({
      id: "E-1",
      status: "ACTIVE",
      remainingSwaps: 2,
    });
    expect(row.status).toBe("ACTIVE");
    expect(row.remainingSwaps).toBe(2);
  });

  it("rejects unknown status", () => {
    expect(() =>
      toSelectableEntitlement({
        id: "E-X",
        status: "WEIRD",
        remainingSwaps: null,
      }),
    ).toThrow(/未知/);
  });
});
