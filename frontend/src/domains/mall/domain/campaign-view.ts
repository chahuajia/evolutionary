/**
 * 营销活动展示模型 — 视图边界。
 *
 * 对齐后端 `Campaign.consumeBudget`：
 * - 仅 ACTIVE 可领券
 * - budgetRemaining >= face
 */

export type CampaignStatus = "DRAFT" | "ACTIVE" | "ENDED";

const CAMPAIGN_STATUSES = ["DRAFT", "ACTIVE", "ENDED"] as const;

export function parseCampaignStatus(raw: unknown): CampaignStatus {
  if (
    typeof raw === "string" &&
    (CAMPAIGN_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as CampaignStatus;
  }
  throw new Error(
    `未知活动状态：${String(raw)}（契约：${CAMPAIGN_STATUSES.join(" | ")}）`,
  );
}

export type CampaignView = {
  readonly id: string;
  readonly ownerOrgId: string;
  readonly name: string;
  readonly budgetRemainingCents: number;
  readonly status: CampaignStatus;
  readonly statusLabel: string;
  readonly claimAllowed: boolean;
  readonly blockMessage: string | null;
};

export const CAMPAIGN_STATUS_LABEL: Record<CampaignStatus, string> = {
  DRAFT: "草稿",
  ACTIVE: "进行中",
  ENDED: "已结束",
};

/** 展示不变量：仅 ACTIVE 可领券。 */
export function canClaimFromCampaign(status: CampaignStatus): boolean {
  return status === "ACTIVE";
}

/** 展示不变量：剩余预算能否覆盖面额。 */
export function hasCampaignBudget(
  budgetRemainingCents: number,
  faceCents: number,
): boolean {
  if (faceCents < 0) return false;
  return budgetRemainingCents >= faceCents;
}

/**
 * 领券总门：ACTIVE ∧ 预算够。
 * faceCents 未知时传 0（只卡状态）。
 */
export function canClaimCoupon(
  status: CampaignStatus,
  budgetRemainingCents: number,
  faceCents: number = 0,
): boolean {
  if (!canClaimFromCampaign(status)) return false;
  return hasCampaignBudget(budgetRemainingCents, faceCents);
}

export function campaignClaimBlockMessage(
  status: CampaignStatus,
  budgetRemainingCents: number,
  faceCents: number = 0,
): string | null {
  if (!canClaimFromCampaign(status)) {
    if (status === "DRAFT") return "活动未激活，不可领券";
    return "活动已结束，不可领券";
  }
  if (!hasCampaignBudget(budgetRemainingCents, faceCents)) {
    return "活动预算已耗尽，不可领券";
  }
  return null;
}

export function toCampaignView(
  dto: {
    id: string;
    ownerOrgId: string;
    name: string;
    budgetRemainingCents: number;
    status: CampaignStatus;
  },
  faceCents: number = 0,
): CampaignView {
  return {
    id: dto.id,
    ownerOrgId: dto.ownerOrgId,
    name: dto.name,
    budgetRemainingCents: dto.budgetRemainingCents,
    status: dto.status,
    statusLabel: CAMPAIGN_STATUS_LABEL[dto.status],
    claimAllowed: canClaimCoupon(
      dto.status,
      dto.budgetRemainingCents,
      faceCents,
    ),
    blockMessage: campaignClaimBlockMessage(
      dto.status,
      dto.budgetRemainingCents,
      faceCents,
    ),
  };
}
