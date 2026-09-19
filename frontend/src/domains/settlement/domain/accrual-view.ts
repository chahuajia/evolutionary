/**
 * 应计展示模型 — 视图边界。
 */

import { formatCentsAsYuan } from "@/shared/money/format-cents";

export { formatCentsAsYuan };

export type AccrualView = {
  readonly id: string;
  readonly orderId: string;
  readonly orgId: string;
  readonly amountCents: number;
  readonly status: string;
  readonly amountYuan: string;
};

export function toAccrualView(dto: {
  id: string;
  orderId: string;
  orgId: string;
  amountCents: number;
  status: string;
}): AccrualView {
  return {
    id: dto.id,
    orderId: dto.orderId,
    orgId: dto.orgId,
    amountCents: dto.amountCents,
    status: dto.status,
    amountYuan: formatCentsAsYuan(dto.amountCents),
  };
}
