/**
 * 用例：加载券模板面额（编排 gateway → CouponTemplateView）。
 */

import {
  toCouponTemplateView,
  type CouponTemplateView,
} from "@/domains/mall/domain/coupon-template-view";
import {
  DEFAULT_MALL_TEMPLATE,
  fetchCouponTemplate,
} from "@/domains/mall/infrastructure/mall-gateway";

export type { CouponTemplateView };

export async function loadCouponTemplate(
  templateId: string = DEFAULT_MALL_TEMPLATE,
): Promise<CouponTemplateView> {
  const dto = await fetchCouponTemplate(templateId);
  return toCouponTemplateView({
    id: dto.id,
    faceBudgetCents: dto.faceBudgetCents,
    kind: dto.kind,
    campaignId: dto.campaignId,
  });
}

export { DEFAULT_MALL_TEMPLATE };
