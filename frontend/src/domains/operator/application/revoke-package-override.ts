/**
 * 用例：L2 撤销套餐覆盖（编排 gateway，不写 fetch 细节）。
 */

import {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ACTOR_USER_ID,
  DEFAULT_OVERRIDE_ID,
  postRevokePackageOverride,
  type RevokePackageOverrideResult,
} from "@/domains/operator/infrastructure/operator-gateway";

export type RevokePackageOverrideInput = {
  overrideId?: string;
  actorUserId?: string;
  actorOrgId?: string;
};

export async function revokePackageOverride(
  input: RevokePackageOverrideInput = {},
): Promise<RevokePackageOverrideResult> {
  return postRevokePackageOverride({
    overrideId: input.overrideId?.trim() || DEFAULT_OVERRIDE_ID,
    actorUserId:
      input.actorUserId?.trim() || DEFAULT_OVERRIDE_ACTOR_USER_ID,
    actorOrgId: input.actorOrgId?.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  });
}

export {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ACTOR_USER_ID,
  DEFAULT_OVERRIDE_ID,
};
