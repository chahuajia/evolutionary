/**
 * 用例：加载用户券展示模型（编排 gateway → UserCouponView）。
 */

import {
  parseUserCouponStatus,
  toUserCouponView,
  type UserCouponView,
} from "@/domains/mall/domain/user-coupon-view";
import { fetchUserCoupon } from "@/domains/mall/infrastructure/mall-gateway";

export async function loadUserCoupon(
  userCouponId: string,
): Promise<UserCouponView> {
  const dto = await fetchUserCoupon(userCouponId);
  return toUserCouponView({
    id: dto.id,
    userId: dto.userId,
    templateId: dto.templateId,
    status: parseUserCouponStatus(dto.status),
  });
}
