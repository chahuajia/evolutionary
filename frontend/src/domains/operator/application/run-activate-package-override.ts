/**
 * 用例：激活套餐覆盖（编排 gateway → PackageOverrideView）。
 */

import {
  parsePackageOverrideStatus,
  toPackageOverrideView,
  type PackageOverrideView,
} from "@/domains/operator/domain/package-override-view";
import {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ACTOR_USER_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_OVERRIDE_PRICE_CENTS,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  postActivatePackageOverride,
  type ActivatePackageOverrideRequest,
} from "@/domains/operator/infrastructure/operator-gateway";

export type ActivatePackageOverrideView = {
  readonly view: PackageOverrideView;
  readonly priceCents: number | null;
};

export async function runActivatePackageOverride(
  req: ActivatePackageOverrideRequest,
): Promise<ActivatePackageOverrideView> {
  const r = await postActivatePackageOverride({
    templateId: req.templateId.trim() || DEFAULT_OVERRIDE_TEMPLATE_ID,
    actorOrgId: req.actorOrgId.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID,
    actorUserId: req.actorUserId.trim() || DEFAULT_OVERRIDE_ACTOR_USER_ID,
    overrideId: req.overrideId.trim() || DEFAULT_OVERRIDE_ID,
    patches: {
      price:
        req.patches.price != null && Number.isFinite(req.patches.price)
          ? req.patches.price
          : DEFAULT_OVERRIDE_PRICE_CENTS,
      displayName: req.patches.displayName,
    },
  });
  return {
    view: toPackageOverrideView({
      overrideId: r.overrideId,
      orgId: r.orgId,
      templateId: r.templateId,
      templateVersion: r.templateVersion,
      status: parsePackageOverrideStatus(r.status),
    }),
    priceCents: r.priceCents,
  };
}

export {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ACTOR_USER_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_OVERRIDE_PRICE_CENTS,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
};
