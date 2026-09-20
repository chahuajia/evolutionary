import { describe, expect, it } from "vitest";
import { toEntitledSwapView } from "./entitled-swap-view";

describe("toEntitledSwapView", () => {
  it("maps usage event and charged yuan", () => {
    const view = toEntitledSwapView({
      usageEventId: "UE-1",
      status: "COMPLETED",
      batteryId: "B-1",
      cabinetId: "C-1",
      entitlementId: "E-1",
      chargedAmountCents: 250,
    });
    expect(view.usageEvent.id).toBe("UE-1");
    expect(view.chargedAmountYuan).toBe("2.50");
  });

  it("null charge when missing", () => {
    const view = toEntitledSwapView({
      usageEventId: "UE-2",
      status: "COMPLETED",
      batteryId: "B-2",
      cabinetId: "C-2",
      entitlementId: "E-2",
    });
    expect(view.chargedAmountCents).toBeNull();
    expect(view.chargedAmountYuan).toBeNull();
  });
});
