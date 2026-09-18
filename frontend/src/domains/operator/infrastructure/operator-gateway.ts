/**
 * 运维/运营 BC 防腐层 — HTTP gateway（DTO → 前端读模型）
 * 浏览器经 Next `/api` rewrite；RSC 直连 BACKEND_ORIGIN。
 */

import { fetchJson } from "@/shared/http/fetch-json";

/** 与 19a DevSeed 对齐：入驻申请 APP-M1 */
export const DEFAULT_ONBOARDING_APPLICATION_ID = "APP-M1";
export const DEFAULT_SHOP_NAME = "黑鸟旗舰店";

/** 与 23a 发布契约对齐：草稿模板 T-DRAFT-1 / ORG-L1 / U-ADMIN */
export const DEFAULT_PACKAGE_TEMPLATE_ID = "T-DRAFT-1";
export const DEFAULT_PUBLISH_ACTOR_ORG_ID = "ORG-L1";
export const DEFAULT_PUBLISH_ACTOR_USER_ID = "U-ADMIN";

const TIMEOUT_MS = 8000;

function resolveOperatorApiBase(): string {
  if (typeof window === "undefined") {
    return process.env.BACKEND_ORIGIN ?? "http://localhost:8080";
  }
  return process.env.NEXT_PUBLIC_API_BASE ?? "/api";
}

/** POST /operator/onboarding/{applicationId}/approve 成功读模型（AC-40） */
export type ApproveOnboardingResult = {
  orgId: string;
  shopName: string;
  status: string;
};

export type ApproveOnboardingRequest = {
  applicationId: string;
  shopName: string;
};

/**
 * POST /operator/onboarding/{applicationId}/approve
 * body `{ shopName }`；错误经 fetchJson 已拼 suggestion。
 */
export async function postApproveOnboarding(
  req: ApproveOnboardingRequest,
): Promise<ApproveOnboardingResult> {
  const applicationId =
    req.applicationId.trim() || DEFAULT_ONBOARDING_APPLICATION_ID;
  const shopName = req.shopName.trim() || DEFAULT_SHOP_NAME;
  const base = resolveOperatorApiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/operator/onboarding/${encodeURIComponent(applicationId)}/approve`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ shopName }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseApproveOnboarding(raw);
}

function parseApproveOnboarding(
  raw: Record<string, unknown>,
): ApproveOnboardingResult {
  const orgId = String(raw.orgId ?? raw.merchantOrgId ?? "");
  const shopName = String(raw.shopName ?? "");
  const status = String(raw.status ?? "");
  if (!orgId || !status) {
    throw new Error("批准入驻响应缺少 orgId/status");
  }
  return { orgId, shopName, status };
}

/** POST /operator/templates/{templateId}/publish 成功读模型（AC-24） */
export type PublishPackageTemplateResult = {
  templateId: string;
  ownerOrgId: string;
  version: number;
  status: string;
  publishedAt: string | null;
};

export type PublishPackageTemplateRequest = {
  templateId: string;
  actorUserId: string;
  actorOrgId: string;
};

/**
 * POST /operator/templates/{templateId}/publish
 * body `{ actorUserId, actorOrgId }`；错误经 fetchJson 已拼 suggestion。
 */
export async function postPublishPackageTemplate(
  req: PublishPackageTemplateRequest,
): Promise<PublishPackageTemplateResult> {
  const templateId = req.templateId.trim() || DEFAULT_PACKAGE_TEMPLATE_ID;
  const actorUserId =
    req.actorUserId.trim() || DEFAULT_PUBLISH_ACTOR_USER_ID;
  const actorOrgId = req.actorOrgId.trim() || DEFAULT_PUBLISH_ACTOR_ORG_ID;
  const base = resolveOperatorApiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/operator/templates/${encodeURIComponent(templateId)}/publish`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ actorUserId, actorOrgId }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parsePublishPackageTemplate(raw, templateId);
}

function parsePublishPackageTemplate(
  raw: Record<string, unknown>,
  fallbackTemplateId: string,
): PublishPackageTemplateResult {
  const templateId = String(raw.templateId ?? raw.id ?? fallbackTemplateId);
  const ownerOrgId = String(raw.ownerOrgId ?? "");
  const versionRaw = raw.version;
  const version =
    typeof versionRaw === "number"
      ? versionRaw
      : Number.parseInt(String(versionRaw ?? ""), 10);
  const status = String(raw.status ?? "");
  const publishedAtRaw = raw.publishedAt;
  const publishedAt =
    publishedAtRaw == null || publishedAtRaw === ""
      ? null
      : String(publishedAtRaw);
  if (!templateId || !status || !Number.isFinite(version)) {
    throw new Error("发布模板响应缺少 templateId/status/version");
  }
  return { templateId, ownerOrgId, version, status, publishedAt };
}
