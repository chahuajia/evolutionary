/**
 * 用例：加载电池运维工单列表（编排 gateway）。
 */

import {
  DEFAULT_IOT_BATTERY,
  fetchMaintenanceTickets,
  type MaintenanceTicketItem,
} from "@/domains/iot/infrastructure/iot-gateway";

export async function loadTickets(
  batteryId: string = DEFAULT_IOT_BATTERY,
): Promise<readonly MaintenanceTicketItem[]> {
  return fetchMaintenanceTickets(batteryId);
}

export { DEFAULT_IOT_BATTERY };
