/**
 * 用例：加载套餐有效价（编排 gateway → EffectiveProductView）。
 */

import {
  toEffectiveProductView,
  type EffectiveProductView,
} from "@/domains/operator/domain/effective-product-view";
import {
  DEFAULT_DOWNLINE_APPLICATION_ID,
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  DEFAULT_PACKAGE_TEMPLATE_ID,
  getEffectiveProduct,
} from "@/domains/operator/infrastructure/operator-gateway";

export type { EffectiveProductView };

export async function loadEffectiveProduct(
  orgId: string = DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  templateId: string = DEFAULT_OVERRIDE_TEMPLATE_ID,
): Promise<EffectiveProductView> {
  const r = await getEffectiveProduct({ orgId, templateId });
  return toEffectiveProductView(r);
}

export {
  DEFAULT_DOWNLINE_APPLICATION_ID,
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  DEFAULT_PACKAGE_TEMPLATE_ID,
};
