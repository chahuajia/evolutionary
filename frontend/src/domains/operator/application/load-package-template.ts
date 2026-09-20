/**
 * 用例：加载套餐模板展示模型（编排 gateway → PackageTemplateView）。
 */

import {
  parsePackageTemplateStatus,
  toPackageTemplateView,
  type PackageTemplateView,
} from "@/domains/operator/domain/package-template-view";
import {
  DEFAULT_PACKAGE_TEMPLATE_ID,
  fetchPackageTemplate,
} from "@/domains/operator/infrastructure/operator-gateway";

export async function loadPackageTemplate(
  templateId: string = DEFAULT_PACKAGE_TEMPLATE_ID,
): Promise<PackageTemplateView> {
  const dto = await fetchPackageTemplate(templateId);
  return toPackageTemplateView({
    id: dto.templateId,
    ownerOrgId: dto.ownerOrgId,
    version: dto.version,
    status: parsePackageTemplateStatus(dto.status),
  });
}

export { DEFAULT_PACKAGE_TEMPLATE_ID };
