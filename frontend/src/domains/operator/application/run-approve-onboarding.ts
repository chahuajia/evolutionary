/**
 * 用例：批准商家入驻（编排 gateway → MerchantProfileView）。
 */

import {
  parseMerchantProfileStatus,
  toMerchantProfileView,
  type MerchantProfileView,
} from "@/domains/mall/domain/merchant-profile-view";
import {
  DEFAULT_ONBOARDING_APPLICATION_ID,
  DEFAULT_SHOP_NAME,
  postApproveOnboarding,
} from "@/domains/operator/infrastructure/operator-gateway";

export async function runApproveOnboarding(input: {
  applicationId?: string;
  shopName?: string;
}): Promise<MerchantProfileView> {
  const r = await postApproveOnboarding({
    applicationId:
      input.applicationId?.trim() || DEFAULT_ONBOARDING_APPLICATION_ID,
    shopName: input.shopName?.trim() || DEFAULT_SHOP_NAME,
  });
  return toMerchantProfileView({
    orgId: r.orgId,
    shopName: r.shopName,
    status: parseMerchantProfileStatus(r.status),
  });
}

export { DEFAULT_ONBOARDING_APPLICATION_ID, DEFAULT_SHOP_NAME };
