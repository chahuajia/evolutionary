/**
 * 用户优惠券展示模型 —— 视图边界。
 *
 * 对齐后端 `UserCoupon`：
 * - `lock` 仅 AVAILABLE
 * - `markUsed` 允许 LOCKED 或 AVAILABLE
 */

export type UserCouponStatus = "AVAILABLE" | "LOCKED" | "USED" | "EXPIRED";

const USER_COUPON_STATUSES = [
  "AVAILABLE",
  "LOCKED",
  "USED",
  "EXPIRED",
] as const;

/** 边界解析：未知值当场炸。 */
export function parseUserCouponStatus(raw: unknown): UserCouponStatus {
  if (
    typeof raw === "string" &&
    (USER_COUPON_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as UserCouponStatus;
  }
  throw new Error(
    `未知用户券状态：${String(raw)}（契约：${USER_COUPON_STATUSES.join(" | ")}）`,
  );
}

export type UserCouponView = {
  readonly id: string;
  readonly userId: string;
  readonly templateId: string;
  readonly status: UserCouponStatus;
  /** 仅 AVAILABLE 可锁定到订单（对齐 `UserCoupon.lock`）。 */
  readonly lockAllowed: boolean;
  /** LOCKED 或 AVAILABLE 可核销（对齐 `markUsed`）。 */
  readonly redeemAllowed: boolean;
  /** 结账时是否可选用本券。 */
  readonly checkoutSelectable: boolean;
  readonly blockMessage: string | null;
};

/** 展示不变量：仅可用券可锁定。 */
export function canLockCoupon(status: UserCouponStatus): boolean {
  return status === "AVAILABLE";
}

/**
 * 展示不变量：LOCKED 或 AVAILABLE 可核销。
 * 对齐 `UserCoupon.markUsed`。
 */
export function canRedeemCoupon(status: UserCouponStatus): boolean {
  return status === "LOCKED" || status === "AVAILABLE";
}

/**
 * 结账选用：只有 AVAILABLE 才该出现在可选列表。
 * LOCKED 已绑单，USED/EXPIRED 不可再用。
 */
export function canSelectCouponForCheckout(status: UserCouponStatus): boolean {
  return status === "AVAILABLE";
}

export function couponBlockMessage(status: UserCouponStatus): string | null {
  if (status === "AVAILABLE") return null;
  if (status === "LOCKED") return "券已锁定在订单上，不可再选入新结账";
  if (status === "USED") return "券已核销，不可再用";
  return "券已过期，不可再用";
}

export function toUserCouponView(dto: {
  id: string;
  userId: string;
  templateId: string;
  status: UserCouponStatus;
}): UserCouponView {
  return {
    id: dto.id,
    userId: dto.userId,
    templateId: dto.templateId,
    status: dto.status,
    lockAllowed: canLockCoupon(dto.status),
    redeemAllowed: canRedeemCoupon(dto.status),
    checkoutSelectable: canSelectCouponForCheckout(dto.status),
    blockMessage: couponBlockMessage(dto.status),
  };
}
