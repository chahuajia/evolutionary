/**
 * 运维/运营 BC 防腐层 — HTTP gateway（DTO → 前端读模型）
 * 浏览器经 Next `/api` rewrite；RSC 直连 BACKEND_ORIGIN。
 */

import { fetchJson } from "@/shared/http/fetch-json";

/** 与 19a DevSeed 对齐：入驻申请 APP-M1 */
export const DEFAULT_ONBOARDING_APPLICATION_ID = "APP-M1";
export const DEFAULT_SHOP_NAME = "黑鸟旗舰店";

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
