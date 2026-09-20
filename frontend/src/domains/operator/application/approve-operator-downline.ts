/**
 * 用例：运营商批准 OPERATOR 下线入驻（编排 gateway → OrganizationView）。
 */

import {
  toOrganizationView,
  type OrganizationView,
} from "@/domains/operator/domain/organization-view";
import {
  DEFAULT_DOWNLINE_ACTOR_ORG_ID,
  DEFAULT_DOWNLINE_ACTOR_USER_ID,
  DEFAULT_DOWNLINE_APPLICATION_ID,
  postApproveOperatorDownline,
} from "@/domains/operator/infrastructure/operator-gateway";

export type ApproveOperatorDownlineInput = {
  applicationId?: string;
  actorUserId?: string;
  actorOrgId?: string;
};

export async function approveOperatorDownline(
  input: ApproveOperatorDownlineInput = {},
): Promise<OrganizationView> {
  const r = await postApproveOperatorDownline({
    applicationId:
      input.applicationId?.trim() || DEFAULT_DOWNLINE_APPLICATION_ID,
    actorUserId:
      input.actorUserId?.trim() || DEFAULT_DOWNLINE_ACTOR_USER_ID,
    actorOrgId: input.actorOrgId?.trim() || DEFAULT_DOWNLINE_ACTOR_ORG_ID,
  });
  return toOrganizationView({
    id: r.orgId,
    name: r.name,
    parentId: r.parentOrgId,
    status: r.status || "ACTIVE",
    operatorCapability: r.operatorCapability,
  });
}

export {
  DEFAULT_DOWNLINE_ACTOR_ORG_ID,
  DEFAULT_DOWNLINE_ACTOR_USER_ID,
  DEFAULT_DOWNLINE_APPLICATION_ID,
};
