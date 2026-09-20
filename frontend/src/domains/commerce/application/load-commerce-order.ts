/**
 * 用例：加载商城订单展示模型（编排 gateway → CommerceOrderView）。
 */

import {
  parseCommerceOrderStatus,
  toCommerceOrderView,
  type CommerceOrderView,
} from "@/domains/commerce/domain/commerce-order-view";
import { fetchCommerceOrder } from "@/domains/commerce/infrastructure/order-refund-gateway";

export async function loadCommerceOrder(
  orderId: string,
): Promise<CommerceOrderView> {
  const dto = await fetchCommerceOrder(orderId);
  return toCommerceOrderView({
    orderId: dto.orderId,
    status: parseCommerceOrderStatus(dto.status),
  });
}
