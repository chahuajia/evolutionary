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
} from "@/domains/operator/infrastructure/operator-gateway";

export type ActivatePackageOverrideInput = {
  readonly templateId: string;
  readonly actorUserId: string;
  readonly actorOrgId: string;
  readonly overrideId: string;
  readonly patches: { readonly price?: number; readonly displayName?: string };
};

export type { PackageOverrideView };

export async function runActivatePackageOverride(
  req: ActivatePackageOverrideInput,
): Promise<PackageOverrideView> {
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
  return toPackageOverrideView({
    overrideId: r.overrideId,
    orgId: r.orgId,
    templateId: r.templateId,
    templateVersion: r.templateVersion,
    status: parsePackageOverrideStatus(r.status),
    priceCents: r.priceCents,
  });
}

export {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ACTOR_USER_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_OVERRIDE_PRICE_CENTS,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
};
