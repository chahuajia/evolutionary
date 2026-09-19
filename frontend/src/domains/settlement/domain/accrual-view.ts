/**
 * 应计展示模型 — 视图边界。
 */

import { formatCentsAsYuan } from "@/shared/money/format-cents";

export { formatCentsAsYuan };

/**
 * 后端 `ProfitShareAccrual.Status` 的契约。
 *
 * 此前这里是裸 `string`，于是测试里漂出一个**后端从来不存在**的 "ACCRUED"
 * 而没人发现 —— 契约从类型上丢了（见 [[S33-测试数据的契约一致性]]）。
 */
export type AccrualStatus = "PENDING" | "SETTLED" | "REVERSED";

const ACCRUAL_STATUSES = ["PENDING", "SETTLED", "REVERSED"] as const;

/**
 * 边界解析：把外部 `status` 变成契约类型。
 *
 * **未知值在这里炸，不产生默认值。** 原先网关写 `String(r.status ?? "")` ——
 * 未知状态会静默变成 `""` 往下传，直到 UI 上显示成空白才被发现。
 * 对齐 [[patterns/parse-dont-validate]]：边界的职责是 **parse**（合法化），
 * 不是 validate（校验后放行）。
 */
export function parseAccrualStatus(raw: unknown): AccrualStatus {
  if (
    typeof raw === "string" &&
    (ACCRUAL_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as AccrualStatus;
  }
  throw new Error(
    `未知应计状态：${String(raw)}（契约：${ACCRUAL_STATUSES.join(" | ")}）`,
  );
}

export type AccrualView = {
  readonly id: string;
  readonly orderId: string;
  readonly orgId: string;
  readonly amountCents: number;
  readonly status: AccrualStatus;
  readonly amountYuan: string;
  /** 仅 PENDING 可结算（对齐后端 `settle` 守卫）。 */
  readonly settleAllowed: boolean;
  /** 仅 PENDING 可冲销（对齐后端 `reverse` 守卫）。 */
  readonly reverseAllowed: boolean;
  /** 不可动时的原因；可动为 null。 */
  readonly blockMessage: string | null;
};

/**
 * 展示不变量：仅 PENDING 可结算。
 *
 * 对齐后端 `ProfitShareAccrual.settle`：
 * `if (status != PENDING) throw new IllegalStateException("仅 PENDING 可结算")`
 */
export function canSettleAccrual(status: AccrualStatus): boolean {
  return status === "PENDING";
}

/**
 * 展示不变量：仅 PENDING 可冲销。
 *
 * 对齐后端 `ProfitShareAccrual.reverse`：
 * 非 PENDING 抛 `ORDER_NOT_REFUNDABLE_SETTLED`「已结算分润不可退款」。
 */
export function canReverseAccrual(status: AccrualStatus): boolean {
  return status === "PENDING";
}

/**
 * 不可动的原因（供 UI 说明，不让用户对着灰按钮猜）。
 *
 * SETTLED 与 REVERSED 都是终态，但成因不同，措辞必须分开 ——
 * 「已结算」和「已冲销」对用户是两件事。
 */
export function accrualBlockMessage(status: AccrualStatus): string | null {
  if (status === "PENDING") return null;
  if (status === "SETTLED") {
    return "该分润已结算，不可再结算或冲销";
  }
  return "该分润已冲销（退款时反冲），不可再动";
}

export function toAccrualView(dto: {
  id: string;
  orderId: string;
  orgId: string;
  amountCents: number;
  status: AccrualStatus;
}): AccrualView {
  return {
    id: dto.id,
    orderId: dto.orderId,
    orgId: dto.orgId,
    amountCents: dto.amountCents,
    status: dto.status,
    amountYuan: formatCentsAsYuan(dto.amountCents),
    settleAllowed: canSettleAccrual(dto.status),
    reverseAllowed: canReverseAccrual(dto.status),
    blockMessage: accrualBlockMessage(dto.status),
  };
}
