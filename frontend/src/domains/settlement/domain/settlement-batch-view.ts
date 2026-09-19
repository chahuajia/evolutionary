/**
 * 结算批展示模型 — 视图边界。
 *
 * 对齐后端 `SettlementBatch`：
 * - close：仅 OPEN → CLOSED
 * - `POST /settlement/batches`（RunSettlementBatch）一次跑完即返回 CLOSED
 */

export type SettlementBatchStatus = "OPEN" | "CLOSED";

const BATCH_STATUSES = ["OPEN", "CLOSED"] as const;

export function parseSettlementBatchStatus(
  raw: unknown,
): SettlementBatchStatus {
  if (
    typeof raw === "string" &&
    (BATCH_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as SettlementBatchStatus;
  }
  throw new Error(
    `未知结算批状态：${String(raw)}（契约：${BATCH_STATUSES.join(" | ")}）`,
  );
}

export type SettlementBatchView = {
  readonly id: string;
  readonly status: SettlementBatchStatus;
  readonly statusLabel: string;
  readonly periodStart: string;
  readonly periodEnd: string;
  readonly closedAt: string | null;
  /** 仅 OPEN 可关账（对齐 `SettlementBatch.close`）。 */
  readonly closeAllowed: boolean;
  readonly blockMessage: string | null;
};

export const SETTLEMENT_BATCH_STATUS_LABEL: Record<
  SettlementBatchStatus,
  string
> = {
  OPEN: "开账中",
  CLOSED: "已关账",
};

/** 展示不变量：仅 OPEN 可关账。 */
export function canCloseSettlementBatch(
  status: SettlementBatchStatus,
): boolean {
  return status === "OPEN";
}

export function settlementBatchBlockMessage(
  status: SettlementBatchStatus,
): string | null {
  if (status === "OPEN") return null;
  return "结算批已关账，不可再关";
}

export function toSettlementBatchView(dto: {
  id: string;
  status: string;
  periodStart: string;
  periodEnd: string;
  closedAt?: string | null;
}): SettlementBatchView {
  const status = parseSettlementBatchStatus(dto.status);
  return {
    id: dto.id,
    status,
    statusLabel: SETTLEMENT_BATCH_STATUS_LABEL[status],
    periodStart: dto.periodStart,
    periodEnd: dto.periodEnd,
    closedAt: dto.closedAt ?? null,
    closeAllowed: canCloseSettlementBatch(status),
    blockMessage: settlementBatchBlockMessage(status),
  };
}
