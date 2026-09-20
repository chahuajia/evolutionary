/**
 * 用例：派生下一版本草稿（编排 gateway → PackageTemplateView）。
 */

import {
  parsePackageTemplateStatus,
  toPackageTemplateView,
  type PackageTemplateView,
} from "@/domains/operator/domain/package-template-view";
import {
  DEFAULT_NEXT_VERSION_DISPLAY_NAME,
  DEFAULT_NEXT_VERSION_DURATION_DAYS,
  DEFAULT_NEXT_VERSION_NEW_ID,
  DEFAULT_NEXT_VERSION_PRICE_CENTS,
  DEFAULT_NEXT_VERSION_SOURCE_ID,
  DEFAULT_PUBLISH_ACTOR_ORG_ID,
  DEFAULT_PUBLISH_ACTOR_USER_ID,
  postCreateNextVersionDraft,
} from "@/domains/operator/infrastructure/operator-gateway";

export type NextVersionDraftInput = {
  readonly sourceTemplateId: string;
  readonly newTemplateId: string;
  readonly actorUserId: string;
  readonly actorOrgId: string;
  readonly displayName: string;
  readonly priceCents: number;
  readonly durationDays: number;
};

export async function runCreateNextVersionDraft(
  req: NextVersionDraftInput,
): Promise<PackageTemplateView> {
  const r = await postCreateNextVersionDraft({
    sourceTemplateId:
      req.sourceTemplateId.trim() || DEFAULT_NEXT_VERSION_SOURCE_ID,
    newTemplateId: req.newTemplateId.trim() || DEFAULT_NEXT_VERSION_NEW_ID,
    actorOrgId: req.actorOrgId.trim() || DEFAULT_PUBLISH_ACTOR_ORG_ID,
    actorUserId: req.actorUserId.trim() || DEFAULT_PUBLISH_ACTOR_USER_ID,
    displayName: req.displayName.trim() || DEFAULT_NEXT_VERSION_DISPLAY_NAME,
    priceCents: Number.isFinite(req.priceCents)
      ? req.priceCents
      : DEFAULT_NEXT_VERSION_PRICE_CENTS,
    durationDays: Number.isFinite(req.durationDays)
      ? req.durationDays
      : DEFAULT_NEXT_VERSION_DURATION_DAYS,
  });
  return toPackageTemplateView({
    id: r.templateId,
    ownerOrgId: r.ownerOrgId,
    version: r.version,
    status: parsePackageTemplateStatus(r.status),
  });
}

export {
  DEFAULT_NEXT_VERSION_DISPLAY_NAME,
  DEFAULT_NEXT_VERSION_DURATION_DAYS,
  DEFAULT_NEXT_VERSION_NEW_ID,
  DEFAULT_NEXT_VERSION_PRICE_CENTS,
  DEFAULT_NEXT_VERSION_SOURCE_ID,
  DEFAULT_PUBLISH_ACTOR_ORG_ID,
  DEFAULT_PUBLISH_ACTOR_USER_ID,
};
