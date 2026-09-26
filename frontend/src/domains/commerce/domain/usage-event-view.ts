/**
 * 用量事件展示模型 — 视图边界。
 *
 * 对齐后端 `UsageEvent`：
 * - complete / fail：仅 STARTED
 */

export type UsageEventStatus = "STARTED" | "COMPLETED" | "FAILED";

const USAGE_EVENT_STATUSES = ["STARTED", "COMPLETED", "FAILED"] as const;

export function parseUsageEventStatus(raw: unknown): UsageEventStatus {
  if (
    typeof raw === "string" &&
    (USAGE_EVENT_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as UsageEventStatus;
  }
  throw new Error(
    `未知用量事件状态：${String(raw)}（契约：${USAGE_EVENT_STATUSES.join(" | ")}）`,
  );
}

export const USAGE_EVENT_STATUS_LABEL: Record<UsageEventStatus, string> = {
  STARTED: "进行中",
  COMPLETED: "已完成",
  FAILED: "失败",
};

export type UsageEventView = {
  readonly id: string;
  readonly status: UsageEventStatus;
  readonly statusLabel: string;
  /** 仅 STARTED 可 complete / fail。 */
  readonly completeAllowed: boolean;
  readonly failAllowed: boolean;
  readonly blockMessage: string | null;
};

/** 展示不变量：仅 STARTED 可完结。 */
export function canCompleteUsageEvent(status: UsageEventStatus): boolean {
  return status === "STARTED";
}

/** 展示不变量：仅 STARTED 可失败收口。 */
export function canFailUsageEvent(status: UsageEventStatus): boolean {
  return status === "STARTED";
}

export function usageEventBlockMessage(
  status: UsageEventStatus,
): string | null {
  if (status === "STARTED") return null;
  if (status === "COMPLETED") return "用量事件已完成，不可再改";
  return "用量事件已失败，不可再改";
}

export function toUsageEventView(dto: {
  id: string;
  status: string;
}): UsageEventView {
  const status = parseUsageEventStatus(dto.status);
  return {
    id: dto.id,
    status,
    statusLabel: USAGE_EVENT_STATUS_LABEL[status],
    completeAllowed: canCompleteUsageEvent(status),
    failAllowed: canFailUsageEvent(status),
    blockMessage: usageEventBlockMessage(status),
  };
}
