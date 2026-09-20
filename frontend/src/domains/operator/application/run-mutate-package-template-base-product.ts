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
} from "@/domains/operator/infrastructure/operator-gateway";

export type MutateBaseProductInput = {
  readonly templateId: string;
  readonly actorOrgId: string;
  readonly displayName: string;
  readonly priceCents: number;
  readonly durationDays: number;
};

export async function runMutatePackageTemplateBaseProduct(
  req: MutateBaseProductInput,
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
    displayName: r.displayName,
    priceCents: r.priceCents,
    durationDays: r.durationDays,
    inheritedFrom: r.inheritedFrom,
  });
}

export { DEFAULT_PACKAGE_TEMPLATE_ID, DEFAULT_PUBLISH_ACTOR_ORG_ID };
