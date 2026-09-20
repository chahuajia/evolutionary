/**
 * 用例：加载套餐有效价（编排 gateway）。
 */

import {
  DEFAULT_DOWNLINE_APPLICATION_ID,
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  DEFAULT_PACKAGE_TEMPLATE_ID,
  getEffectiveProduct,
  type EffectiveProductResult,
} from "@/domains/operator/infrastructure/operator-gateway";

export async function loadEffectiveProduct(
  orgId: string = DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  templateId: string = DEFAULT_OVERRIDE_TEMPLATE_ID,
): Promise<EffectiveProductResult> {
  return getEffectiveProduct({ orgId, templateId });
}

export type { EffectiveProductResult };
export {
  DEFAULT_DOWNLINE_APPLICATION_ID,
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  DEFAULT_PACKAGE_TEMPLATE_ID,
};
