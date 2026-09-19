/**
 * 用例：加载电池运维工单列表（编排 gateway → 展示模型）。
 */

import { toMaintenanceTicketView } from "@/domains/iot/domain/maintenance-ticket-view";
import type { MaintenanceTicketView } from "@/domains/iot/domain/maintenance-ticket-view";
import {
  DEFAULT_IOT_BATTERY,
  fetchMaintenanceTickets,
} from "@/domains/iot/infrastructure/iot-gateway";

export async function loadTickets(
  batteryId: string = DEFAULT_IOT_BATTERY,
): Promise<readonly MaintenanceTicketView[]> {
  const items = await fetchMaintenanceTickets(batteryId);
  return items.map(toMaintenanceTicketView);
}

export { DEFAULT_IOT_BATTERY };
