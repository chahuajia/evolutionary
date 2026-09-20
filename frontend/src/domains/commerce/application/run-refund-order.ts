/**
 * 用例：订单退款（编排 gateway → RefundOrderView）。
 */

import {
  toRefundOrderView,
  type RefundOrderView,
} from "@/domains/commerce/domain/refund-order-view";
import { postRefundOrder } from "@/domains/commerce/infrastructure/order-refund-gateway";

export type { RefundOrderView };

export async function runRefundOrder(
  orderId: string,
): Promise<RefundOrderView> {
  const r = await postRefundOrder(orderId);
  return toRefundOrderView({
    orderId: r.orderId,
    status: r.status,
    entitlementId: r.entitlementId,
    entitlementStatus: r.entitlementStatus,
  });
}
