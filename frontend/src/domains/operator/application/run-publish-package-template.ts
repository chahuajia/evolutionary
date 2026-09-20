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
  type PublishPackageTemplateRequest,
} from "@/domains/operator/infrastructure/operator-gateway";

export async function runPublishPackageTemplate(
  req: PublishPackageTemplateRequest,
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
