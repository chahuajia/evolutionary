/**
 * 换电订单展示模型 — 视图边界（commerce Order，非 MallOrder）。
 *
 * 对齐后端 `Order`：
 * - pay / cancel：仅 CREATED
 * - refund：仅 PAID
 */

export type CommerceOrderStatus =
  | "CREATED"
  | "PAID"
  | "REFUNDED"
  | "CANCELLED";

const COMMERCE_ORDER_STATUSES = [
  "CREATED",
  "PAID",
  "REFUNDED",
  "CANCELLED",
] as const;

export function parseCommerceOrderStatus(
  raw: unknown,
): CommerceOrderStatus {
  if (
    typeof raw === "string" &&
    (COMMERCE_ORDER_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as CommerceOrderStatus;
  }
  throw new Error(
    `未知换电订单状态：${String(raw)}（契约：${COMMERCE_ORDER_STATUSES.join(" | ")}）`,
  );
}

export type CommerceOrderView = {
  readonly orderId: string;
  readonly status: CommerceOrderStatus;
  readonly statusLabel: string;
  readonly payAllowed: boolean;
  readonly cancelAllowed: boolean;
  readonly refundAllowed: boolean;
  readonly blockMessage: string | null;
};

export const COMMERCE_ORDER_STATUS_LABEL: Record<
  CommerceOrderStatus,
  string
> = {
  CREATED: "已创建",
  PAID: "已支付",
  REFUNDED: "已退款",
  CANCELLED: "已取消",
};

export function canPayCommerceOrder(status: CommerceOrderStatus): boolean {
  return status === "CREATED";
}

export function canCancelCommerceOrder(status: CommerceOrderStatus): boolean {
  return status === "CREATED";
}

export function canRefundCommerceOrder(status: CommerceOrderStatus): boolean {
  return status === "PAID";
}

export function commerceOrderBlockMessage(
  status: CommerceOrderStatus,
): string | null {
  if (status === "CREATED") return null;
  if (status === "PAID") return null;
  if (status === "REFUNDED") return "订单已退款，不可再退";
  return "订单已取消，不可支付或退款";
}

export function toCommerceOrderView(dto: {
  orderId: string;
  status: CommerceOrderStatus;
}): CommerceOrderView {
  return {
    orderId: dto.orderId,
    status: dto.status,
    statusLabel: COMMERCE_ORDER_STATUS_LABEL[dto.status],
    payAllowed: canPayCommerceOrder(dto.status),
    cancelAllowed: canCancelCommerceOrder(dto.status),
    refundAllowed: canRefundCommerceOrder(dto.status),
    blockMessage: commerceOrderBlockMessage(dto.status),
  };
}
