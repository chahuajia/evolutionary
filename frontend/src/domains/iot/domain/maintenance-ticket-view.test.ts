import { describe, expect, it } from "vitest";
import {
  canResolveTicket,
  parseMaintenanceTicketStatus,
  ticketNeedsAction,
  toMaintenanceTicketView,
} from "./maintenance-ticket-view";

describe("parseMaintenanceTicketStatus", () => {
  it("accepts OPEN / RESOLVED", () => {
    expect(parseMaintenanceTicketStatus("OPEN")).toBe("OPEN");
    expect(parseMaintenanceTicketStatus("RESOLVED")).toBe("RESOLVED");
  });

  it("throws on unknown", () => {
    expect(() => parseMaintenanceTicketStatus("CLOSED")).toThrow(
      /未知工单状态/,
    );
  });
});

describe("canResolveTicket / ticketNeedsAction", () => {
  it("only OPEN", () => {
    expect(canResolveTicket("OPEN")).toBe(true);
    expect(canResolveTicket("RESOLVED")).toBe(false);
    expect(ticketNeedsAction("OPEN")).toBe(true);
    expect(ticketNeedsAction("RESOLVED")).toBe(false);
  });
});

describe("toMaintenanceTicketView", () => {
  it("OPEN needs action", () => {
    const view = toMaintenanceTicketView({
      ticketId: "T-1",
      batteryId: "BAT-1",
      alertType: "COMM_LOST",
      status: "OPEN",
    });
    expect(view.needsAction).toBe(true);
    expect(view.resolveAllowed).toBe(true);
    expect(view.statusLabel).toBe("待处理");
    expect(view.blockMessage).toBeNull();
  });

  it("RESOLVED is closed", () => {
    const view = toMaintenanceTicketView({
      ticketId: "T-1",
      batteryId: "BAT-1",
      alertType: "COMM_LOST",
      status: "RESOLVED",
    });
    expect(view.needsAction).toBe(false);
    expect(view.blockMessage).toContain("已解决");
  });
});
