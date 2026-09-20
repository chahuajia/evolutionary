/**
 * 退款结果展示模型 — 订单 View + 权益摘要。
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

export type RefundOrderView = {
  readonly order: CommerceOrderView;
  readonly entitlementId: string | null;
  readonly entitlementLabel: string;
};

export function toRefundOrderView(dto: {
  orderId: string;
  status: unknown;
  entitlementId?: string | null;
  entitlementStatus?: string | null;
}): RefundOrderView {
  const status = parseCommerceOrderStatus(dto.status);
  const order = toCommerceOrderView({ orderId: dto.orderId, status });
  const entitlementId = dto.entitlementId ?? null;
  let entitlementLabel = entitlementId ?? "(revoked)";
  if (dto.entitlementStatus) {
    try {
      const ev = toEntitlementView({
        id: entitlementId ?? "?",
        status: parseEntitlementStatus(dto.entitlementStatus),
      });
      entitlementLabel = `${ev.id}/${ev.statusLabel}`;
    } catch {
      entitlementLabel = `${entitlementLabel}/${dto.entitlementStatus}`;
    }
  }
  return { order, entitlementId, entitlementLabel };
}
