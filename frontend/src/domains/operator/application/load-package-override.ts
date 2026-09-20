/**
 * 用例：加载套餐覆盖展示模型（编排 gateway → PackageOverrideView）。
 */

import {
  parsePackageOverrideStatus,
  toPackageOverrideView,
  type PackageOverrideView,
} from "@/domains/operator/domain/package-override-view";
import {
  DEFAULT_OVERRIDE_ID,
  fetchPackageOverride,
} from "@/domains/operator/infrastructure/operator-gateway";

export type { PackageOverrideView };

export async function loadPackageOverride(
  overrideId: string = DEFAULT_OVERRIDE_ID,
): Promise<PackageOverrideView> {
  const dto = await fetchPackageOverride(overrideId);
  return toPackageOverrideView({
    overrideId: dto.overrideId,
    orgId: dto.orgId,
    templateId: dto.templateId,
    templateVersion: dto.templateVersion,
    status: parsePackageOverrideStatus(dto.status),
    priceCents: dto.priceCents,
  });
}

export { DEFAULT_OVERRIDE_ID };
