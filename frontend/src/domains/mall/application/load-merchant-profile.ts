/**
 * 用例：加载商家档案展示模型（编排 gateway → MerchantProfileView）。
 */

import {
  parseMerchantProfileStatus,
  toMerchantProfileView,
  type MerchantProfileView,
} from "@/domains/mall/domain/merchant-profile-view";
import {
  DEFAULT_MALL_MERCHANT,
  fetchMerchantProfile,
} from "@/domains/mall/infrastructure/mall-gateway";

export async function loadMerchantProfile(
  orgId: string = DEFAULT_MALL_MERCHANT,
): Promise<MerchantProfileView> {
  const dto = await fetchMerchantProfile(orgId);
  return toMerchantProfileView({
    orgId: dto.orgId,
    shopName: dto.shopName,
    status: parseMerchantProfileStatus(dto.status),
  });
}

export { DEFAULT_MALL_MERCHANT };
