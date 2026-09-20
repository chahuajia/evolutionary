/**
 * 用例：加载券模板面额（编排 gateway，供领券门 faceCents）。
 */

import {
  DEFAULT_MALL_TEMPLATE,
  fetchCouponTemplate,
} from "@/domains/mall/infrastructure/mall-gateway";

export type CouponTemplateLoad = {
  readonly id: string;
  readonly faceBudgetCents: number;
  readonly kind: string;
  readonly campaignId: string;
};

export async function loadCouponTemplate(
  templateId: string = DEFAULT_MALL_TEMPLATE,
): Promise<CouponTemplateLoad> {
  const dto = await fetchCouponTemplate(templateId);
  return {
    id: dto.id,
    faceBudgetCents: dto.faceBudgetCents,
    kind: dto.kind,
    campaignId: dto.campaignId,
  };
}

export { DEFAULT_MALL_TEMPLATE };
