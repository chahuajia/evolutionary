/**
 * 商城订单展示模型 — 视图边界。
 *
 * 对齐后端 `MallOrder`：
 * - `pay` 仅 CREATED → PAID
 * - INV-16：PAID 后禁止产生 Entitlement
 *
 * 枚举有五态，但领域**只有** `pay` 这一条转移；不发明 ship/complete 门。
 */

import { formatCentsAsYuan } from "@/shared/money/format-cents";

export { formatCentsAsYuan };

export type MallOrderStatus =
  | "CREATED"
  | "PAID"
  | "SHIPPED"
  | "COMPLETED"
  | "REFUNDED";

const MALL_ORDER_STATUSES = [
  "CREATED",
  "PAID",
  "SHIPPED",
  "COMPLETED",
  "REFUNDED",
] as const;

export function parseMallOrderStatus(raw: unknown): MallOrderStatus {
  if (
    typeof raw === "string" &&
    (MALL_ORDER_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as MallOrderStatus;
  }
  throw new Error(
    `未知商城订单状态：${String(raw)}（契约：${MALL_ORDER_STATUSES.join(" | ")}）`,
  );
}

export type MallOrderView = {
  readonly orderId: string;
  readonly userId: string;
  readonly merchantOrgId: string;
  readonly status: MallOrderStatus;
  readonly statusLabel: string;
  readonly paidAmountCents: number;
  readonly paidAmountYuan: string;
  readonly skuId: string;
  readonly qty: number;
  /** 仅 CREATED 可标记支付（对齐 `MallOrder.pay`）。 */
  readonly payAllowed: boolean;
  /** PAID 及之后禁止开换电权益（INV-16）。 */
  readonly entitlementForbidden: boolean;
  readonly blockMessage: string | null;
};

export const MALL_ORDER_STATUS_LABEL: Record<MallOrderStatus, string> = {
  CREATED: "待支付",
  PAID: "已支付",
  SHIPPED: "已发货",
  COMPLETED: "已完成",
  REFUNDED: "已退款",
};

/** 展示不变量：仅 CREATED 可 pay。 */
export function canMarkMallOrderPaid(status: MallOrderStatus): boolean {
  return status === "CREATED";
}

/**
 * INV-16：PAID 后禁止产生 Entitlement。
 * CREATED 也不是换电路径，但守卫原文写的是「PAID 后」。
 */
export function forbidsEntitlement(status: MallOrderStatus): boolean {
  return status !== "CREATED";
}

export function mallOrderBlockMessage(
  status: MallOrderStatus,
): string | null {
  if (status === "CREATED") return null;
  if (status === "PAID") {
    return "已支付 —— 履约走物流/自提，不可开换电权益（INV-16）";
  }
  if (status === "REFUNDED") return "订单已退款";
  return "订单已进入履约/完成态，不可再标记支付";
}

export function toMallOrderView(dto: {
  orderId: string;
  userId: string;
  merchantOrgId: string;
  status: string;
  paidAmountCents: number;
  skuId: string;
  qty: number;
}): MallOrderView {
  const status = parseMallOrderStatus(dto.status);
  return {
    orderId: dto.orderId,
    userId: dto.userId,
    merchantOrgId: dto.merchantOrgId,
    status,
    statusLabel: MALL_ORDER_STATUS_LABEL[status],
    paidAmountCents: dto.paidAmountCents,
    paidAmountYuan: formatCentsAsYuan(dto.paidAmountCents),
    skuId: dto.skuId,
    qty: dto.qty,
    payAllowed: canMarkMallOrderPaid(status),
    entitlementForbidden: forbidsEntitlement(status),
    blockMessage: mallOrderBlockMessage(status),
  };
}
