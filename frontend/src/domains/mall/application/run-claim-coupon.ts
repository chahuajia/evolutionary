/**
 * 用例：活动领券（编排 gateway → UserCouponView）。
 */

import {
  parseUserCouponStatus,
  toUserCouponView,
  type UserCouponView,
} from "@/domains/mall/domain/user-coupon-view";
import {
  DEFAULT_MALL_CAMPAIGN,
  DEFAULT_MALL_TEMPLATE,
  DEFAULT_MALL_USER,
  postClaimCoupon,
  type ClaimCouponRequest,
} from "@/domains/mall/infrastructure/mall-gateway";

export async function runClaimCoupon(
  req: ClaimCouponRequest,
): Promise<UserCouponView> {
  const r = await postClaimCoupon({
    campaignId: req.campaignId?.trim() || DEFAULT_MALL_CAMPAIGN,
    userId: req.userId.trim() || DEFAULT_MALL_USER,
    templateId: req.templateId.trim() || DEFAULT_MALL_TEMPLATE,
  });
  return toUserCouponView({
    id: r.id,
    userId: r.userId,
    templateId: r.templateId,
    status: parseUserCouponStatus(r.status),
  });
}

export {
  DEFAULT_MALL_CAMPAIGN,
  DEFAULT_MALL_TEMPLATE,
  DEFAULT_MALL_USER,
};
