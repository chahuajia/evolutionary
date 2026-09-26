/**
 * 用例：解决运维工单（编排 gateway → MaintenanceTicketView）。
 */

import {
  toMaintenanceTicketView,
  type MaintenanceTicketView,
} from "@/domains/iot/domain/maintenance-ticket-view";
import { postResolveMaintenanceTicket } from "@/domains/iot/infrastructure/iot-gateway";

export async function runResolveMaintenanceTicket(
  ticketId: string,
): Promise<MaintenanceTicketView> {
  const dto = await postResolveMaintenanceTicket(ticketId);
  return toMaintenanceTicketView(dto);
}
