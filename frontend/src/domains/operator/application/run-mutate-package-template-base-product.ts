/**
 * 用例：草稿态改基产品（编排 gateway → PackageTemplateView）。
 */

import {
  parsePackageTemplateStatus,
  toPackageTemplateView,
  type PackageTemplateView,
} from "@/domains/operator/domain/package-template-view";
import {
  DEFAULT_PACKAGE_TEMPLATE_ID,
  DEFAULT_PUBLISH_ACTOR_ORG_ID,
  postMutatePackageTemplateBaseProduct,
  type MutateBaseProductRequest,
} from "@/domains/operator/infrastructure/operator-gateway";

export async function runMutatePackageTemplateBaseProduct(
  req: MutateBaseProductRequest,
): Promise<PackageTemplateView> {
  const r = await postMutatePackageTemplateBaseProduct({
    templateId: req.templateId.trim() || DEFAULT_PACKAGE_TEMPLATE_ID,
    actorOrgId: req.actorOrgId.trim() || DEFAULT_PUBLISH_ACTOR_ORG_ID,
    displayName: req.displayName,
    priceCents: req.priceCents,
    durationDays: req.durationDays,
  });
  return toPackageTemplateView({
    id: r.templateId,
    ownerOrgId: r.ownerOrgId,
    version: r.version,
    status: parsePackageTemplateStatus(r.status),
  });
}

export { DEFAULT_PACKAGE_TEMPLATE_ID, DEFAULT_PUBLISH_ACTOR_ORG_ID };
