/**
 * 用例：发布套餐模板（编排 gateway → PackageTemplateView）。
 */

import {
  parsePackageTemplateStatus,
  toPackageTemplateView,
  type PackageTemplateView,
} from "@/domains/operator/domain/package-template-view";
import {
  DEFAULT_PACKAGE_TEMPLATE_ID,
  DEFAULT_PUBLISH_ACTOR_ORG_ID,
  DEFAULT_PUBLISH_ACTOR_USER_ID,
  postPublishPackageTemplate,
} from "@/domains/operator/infrastructure/operator-gateway";

export type PublishPackageTemplateInput = {
  readonly templateId: string;
  readonly actorUserId: string;
  readonly actorOrgId: string;
};

export async function runPublishPackageTemplate(
  req: PublishPackageTemplateInput,
): Promise<PackageTemplateView> {
  const r = await postPublishPackageTemplate({
    templateId: req.templateId.trim() || DEFAULT_PACKAGE_TEMPLATE_ID,
    actorOrgId: req.actorOrgId.trim() || DEFAULT_PUBLISH_ACTOR_ORG_ID,
    actorUserId: req.actorUserId.trim() || DEFAULT_PUBLISH_ACTOR_USER_ID,
  });
  return toPackageTemplateView({
    id: r.templateId,
    ownerOrgId: r.ownerOrgId,
    version: r.version,
    status: parsePackageTemplateStatus(r.status),
  });
}

export {
  DEFAULT_PACKAGE_TEMPLATE_ID,
  DEFAULT_PUBLISH_ACTOR_ORG_ID,
  DEFAULT_PUBLISH_ACTOR_USER_ID,
};
