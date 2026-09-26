/**
 * 商家档案展示模型 — 视图边界。
 *
 * 对齐后端 `MerchantProfile.isActive`：仅 ACTIVE 可交易。
 */

export type MerchantProfileStatus = "ACTIVE" | "SUSPENDED";

const MERCHANT_PROFILE_STATUSES = ["ACTIVE", "SUSPENDED"] as const;

export function parseMerchantProfileStatus(
  raw: unknown,
): MerchantProfileStatus {
  if (
    typeof raw === "string" &&
    (MERCHANT_PROFILE_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as MerchantProfileStatus;
  }
  throw new Error(
    `未知商家档案状态：${String(raw)}（契约：${MERCHANT_PROFILE_STATUSES.join(" | ")}）`,
  );
}

export type MerchantProfileView = {
  readonly orgId: string;
  readonly shopName: string;
  readonly status: MerchantProfileStatus;
  readonly statusLabel: string;
  /** 仅 ACTIVE 可下单/结账（对齐 isActive）。 */
  readonly tradeAllowed: boolean;
  readonly blockMessage: string | null;
};

export const MERCHANT_STATUS_LABEL: Record<MerchantProfileStatus, string> = {
  ACTIVE: "营业中",
  SUSPENDED: "已停用",
};

/** 展示不变量：仅 ACTIVE 可交易。 */
export function canTradeWithMerchant(status: MerchantProfileStatus): boolean {
  return status === "ACTIVE";
}

export function merchantTradeBlockMessage(
  status: MerchantProfileStatus,
): string | null {
  if (status === "ACTIVE") return null;
  return "商家已停用，不可下单";
}

export function toMerchantProfileView(dto: {
  orgId: string;
  shopName: string;
  status: MerchantProfileStatus;
}): MerchantProfileView {
  return {
    orgId: dto.orgId,
    shopName: dto.shopName,
    status: dto.status,
    statusLabel: MERCHANT_STATUS_LABEL[dto.status],
    tradeAllowed: canTradeWithMerchant(dto.status),
    blockMessage: merchantTradeBlockMessage(dto.status),
  };
}
