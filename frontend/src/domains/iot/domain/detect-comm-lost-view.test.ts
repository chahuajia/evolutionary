import { describe, expect, it } from "vitest";
import {
  parseAlertType,
  toDetectCommLostView,
} from "./detect-comm-lost-view";

describe("parseAlertType", () => {
  it("accepts contract values", () => {
    expect(parseAlertType("COMM_LOST")).toBe("COMM_LOST");
    expect(parseAlertType("OVERHEAT")).toBe("OVERHEAT");
  });

  it("throws on unknown", () => {
    expect(() => parseAlertType("UNKNOWN")).toThrow(/未知告警类型/);
  });
});

describe("toDetectCommLostView", () => {
  it("maps raised COMM_LOST without inventing when alertType null", () => {
    const view = toDetectCommLostView({
      batteryId: "B1",
      stale: true,
      raised: true,
      alertType: null,
      ticketId: "T1",
    });
    expect(view.alertType).toBeNull();
    expect(view.alertTypeLabel).toBeNull();
    expect(view.raisedLabel).toBe("已抬告警");
    expect(view.staleLabel).toBe("过期");
    expect(view.detectUseful).toBe(true);
  });

  it("labels COMM_LOST when present", () => {
    const view = toDetectCommLostView({
      batteryId: "B1",
      stale: true,
      raised: true,
      alertType: "COMM_LOST",
      ticketId: "T1",
    });
    expect(view.alertTypeLabel).toBe("通信丢失");
  });
});
