/**
 * 用例：加载营销活动展示模型（编排 gateway → CampaignView）。
 */

import {
  parseCampaignStatus,
  toCampaignView,
  type CampaignView,
} from "@/domains/mall/domain/campaign-view";
import {
  DEFAULT_MALL_CAMPAIGN,
  fetchCampaign,
} from "@/domains/mall/infrastructure/mall-gateway";

export async function loadCampaign(
  campaignId: string = DEFAULT_MALL_CAMPAIGN,
  faceCents: number = 0,
): Promise<CampaignView> {
  const dto = await fetchCampaign(campaignId);
  return toCampaignView(
    {
      id: dto.id,
      ownerOrgId: dto.ownerOrgId,
      name: dto.name,
      budgetRemainingCents: dto.budgetRemainingCents,
      status: parseCampaignStatus(dto.status),
    },
    faceCents,
  );
}

export { DEFAULT_MALL_CAMPAIGN };
