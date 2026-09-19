/**
 * 入驻申请展示模型 — 视图边界。
 *
 * 对齐后端 `OnboardingApplication.approve`：仅 SUBMITTED 可批准。
 */

export type OnboardingStatus = "SUBMITTED" | "APPROVED" | "REJECTED";

const ONBOARDING_STATUSES = ["SUBMITTED", "APPROVED", "REJECTED"] as const;

export function parseOnboardingStatus(raw: unknown): OnboardingStatus {
  if (
    typeof raw === "string" &&
    (ONBOARDING_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as OnboardingStatus;
  }
  throw new Error(
    `未知入驻申请状态：${String(raw)}（契约：${ONBOARDING_STATUSES.join(" | ")}）`,
  );
}

export type OnboardingApplicationView = {
  readonly id: string;
  readonly orgId: string;
  readonly capability: string;
  readonly status: OnboardingStatus;
  readonly statusLabel: string;
  /** 仅 SUBMITTED 可批准（对齐 `approve`）。 */
  readonly approveAllowed: boolean;
  readonly blockMessage: string | null;
};

export const ONBOARDING_STATUS_LABEL: Record<OnboardingStatus, string> = {
  SUBMITTED: "已提交 · 待审批",
  APPROVED: "已批准",
  REJECTED: "已拒绝",
};

/** 展示不变量：仅 submitted 可批准。 */
export function canApproveOnboarding(status: OnboardingStatus): boolean {
  return status === "SUBMITTED";
}

export function onboardingBlockMessage(
  status: OnboardingStatus,
): string | null {
  if (status === "SUBMITTED") return null;
  if (status === "APPROVED") return "申请已批准，不可再批";
  return "申请已拒绝，不可批准";
}

export function toOnboardingApplicationView(dto: {
  id: string;
  orgId: string;
  capability: string;
  status: OnboardingStatus;
}): OnboardingApplicationView {
  return {
    id: dto.id,
    orgId: dto.orgId,
    capability: dto.capability,
    status: dto.status,
    statusLabel: ONBOARDING_STATUS_LABEL[dto.status],
    approveAllowed: canApproveOnboarding(dto.status),
    blockMessage: onboardingBlockMessage(dto.status),
  };
}
