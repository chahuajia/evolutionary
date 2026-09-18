/**
 * 用例：运营商批准 OPERATOR 下线入驻（编排 gateway，不写 fetch 细节）。
 */

import {
  DEFAULT_DOWNLINE_ACTOR_ORG_ID,
  DEFAULT_DOWNLINE_ACTOR_USER_ID,
  DEFAULT_DOWNLINE_APPLICATION_ID,
  postApproveOperatorDownline,
  type ApproveOperatorDownlineResult,
} from "@/domains/operator/infrastructure/operator-gateway";

export type ApproveOperatorDownlineInput = {
  applicationId?: string;
  actorUserId?: string;
  actorOrgId?: string;
};

export async function approveOperatorDownline(
  input: ApproveOperatorDownlineInput = {},
): Promise<ApproveOperatorDownlineResult> {
  return postApproveOperatorDownline({
    applicationId:
      input.applicationId?.trim() || DEFAULT_DOWNLINE_APPLICATION_ID,
    actorUserId:
      input.actorUserId?.trim() || DEFAULT_DOWNLINE_ACTOR_USER_ID,
    actorOrgId: input.actorOrgId?.trim() || DEFAULT_DOWNLINE_ACTOR_ORG_ID,
  });
}

export {
  DEFAULT_DOWNLINE_ACTOR_ORG_ID,
  DEFAULT_DOWNLINE_ACTOR_USER_ID,
  DEFAULT_DOWNLINE_APPLICATION_ID,
};
