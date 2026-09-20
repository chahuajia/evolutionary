/**
 * 用例：订单退款（编排 gateway → CommerceOrderView + 权益摘要）。
 */

import {
  parseCommerceOrderStatus,
  toCommerceOrderView,
  type CommerceOrderView,
} from "@/domains/commerce/domain/commerce-order-view";
import {
  parseEntitlementStatus,
  toEntitlementView,
} from "@/domains/commerce/domain/entitlement-view";
import { postRefundOrder } from "@/domains/commerce/infrastructure/order-refund-gateway";

export type RefundOrderView = {
  readonly order: CommerceOrderView;
  readonly entitlementId: string | null;
  readonly entitlementLabel: string;
};

export async function runRefundOrder(
  orderId: string,
): Promise<RefundOrderView> {
  const r = await postRefundOrder(orderId);
  const status = parseCommerceOrderStatus(r.status);
  const order = toCommerceOrderView({ orderId: r.orderId, status });
  let entitlementLabel = r.entitlementId ?? "(revoked)";
  if (r.entitlementStatus) {
    try {
      const ev = toEntitlementView({
        id: r.entitlementId ?? "?",
        status: parseEntitlementStatus(r.entitlementStatus),
      });
      entitlementLabel = `${ev.id}/${ev.statusLabel}`;
    } catch {
      entitlementLabel = `${entitlementLabel}/${r.entitlementStatus}`;
    }
  }
  return {
    order,
    entitlementId: r.entitlementId ?? null,
    entitlementLabel,
  };
}
