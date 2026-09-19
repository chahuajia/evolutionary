/**
 * 运维工单展示模型 — 视图边界。
 *
 * 对齐后端 `MaintenanceTicket`：
 * - `isOpen` ↔ OPEN
 * - `resolve`：已 RESOLVED 则幂等返回自身（展示上「无需再解」）
 */

export type MaintenanceTicketStatus = "OPEN" | "RESOLVED";

const TICKET_STATUSES = ["OPEN", "RESOLVED"] as const;

export function parseMaintenanceTicketStatus(
  raw: unknown,
): MaintenanceTicketStatus {
  if (
    typeof raw === "string" &&
    (TICKET_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as MaintenanceTicketStatus;
  }
  throw new Error(
    `未知工单状态：${String(raw)}（契约：${TICKET_STATUSES.join(" | ")}）`,
  );
}

export type MaintenanceTicketView = {
  readonly ticketId: string;
  readonly batteryId: string;
  readonly alertType: string;
  readonly status: MaintenanceTicketStatus;
  readonly statusLabel: string;
  /** 仅 OPEN 需要处理（对齐 isOpen）。 */
  readonly needsAction: boolean;
  /** 仅 OPEN 可点「解决」（已 RESOLVED 再解是空操作）。 */
  readonly resolveAllowed: boolean;
  readonly blockMessage: string | null;
};

export const TICKET_STATUS_LABEL: Record<MaintenanceTicketStatus, string> = {
  OPEN: "待处理",
  RESOLVED: "已解决",
};

/** 展示不变量：仅 OPEN 仍需处理。 */
export function ticketNeedsAction(status: MaintenanceTicketStatus): boolean {
  return status === "OPEN";
}

/** 展示不变量：仅 OPEN 提供解决动作。 */
export function canResolveTicket(status: MaintenanceTicketStatus): boolean {
  return status === "OPEN";
}

export function ticketBlockMessage(
  status: MaintenanceTicketStatus,
): string | null {
  if (status === "OPEN") return null;
  return "工单已解决，无需再操作";
}

export function toMaintenanceTicketView(dto: {
  ticketId: string;
  batteryId: string;
  alertType: string;
  status: string;
}): MaintenanceTicketView {
  const status = parseMaintenanceTicketStatus(dto.status);
  return {
    ticketId: dto.ticketId,
    batteryId: dto.batteryId,
    alertType: dto.alertType,
    status,
    statusLabel: TICKET_STATUS_LABEL[status],
    needsAction: ticketNeedsAction(status),
    resolveAllowed: canResolveTicket(status),
    blockMessage: ticketBlockMessage(status),
  };
}
