/**
 * 用例：L2 撤销套餐覆盖（编排 gateway → PackageOverrideView）。
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
  postRevokePackageOverride,
} from "@/domains/operator/infrastructure/operator-gateway";

export type RevokePackageOverrideInput = {
  overrideId?: string;
  actorUserId?: string;
  actorOrgId?: string;
  /** 撤销响应无版本字段时沿用当前展示版本。 */
  templateVersion?: number;
};

export type RevokePackageOverrideView = {
  readonly view: PackageOverrideView;
  readonly priceCents: number | null;
};

export async function revokePackageOverride(
  input: RevokePackageOverrideInput = {},
): Promise<RevokePackageOverrideView> {
  const r = await postRevokePackageOverride({
    overrideId: input.overrideId?.trim() || DEFAULT_OVERRIDE_ID,
    actorUserId:
      input.actorUserId?.trim() || DEFAULT_OVERRIDE_ACTOR_USER_ID,
    actorOrgId: input.actorOrgId?.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  });
  return {
    view: toPackageOverrideView({
      overrideId: r.overrideId,
      orgId: r.orgId ?? "",
      templateId: r.templateId ?? "",
      templateVersion: input.templateVersion ?? 0,
      status: parsePackageOverrideStatus(r.status),
    }),
    priceCents: r.priceCents ?? null,
  };
}

export {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ACTOR_USER_ID,
  DEFAULT_OVERRIDE_ID,
};
