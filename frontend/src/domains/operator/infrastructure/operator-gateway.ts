/**
 * 运维/运营 BC 防腐层 — HTTP gateway（DTO → 前端读模型）
 * 浏览器经 Next `/api` rewrite；RSC 直连 BACKEND_ORIGIN。
 */

import { apiBase } from "@/shared/http/api-base";
import { fetchJson } from "@/shared/http/fetch-json";

/** 与 19a DevSeed 对齐：入驻申请 APP-M1 */
export const DEFAULT_ONBOARDING_APPLICATION_ID = "APP-M1";
export const DEFAULT_SHOP_NAME = "黑鸟旗舰店";

/** 与 29a 下线批契约对齐：APP-DL1（OPERATOR · ORG-DL1）/ ORG-L1 / U-ADMIN */
export const DEFAULT_DOWNLINE_APPLICATION_ID = "APP-DL1";
export const DEFAULT_DOWNLINE_ACTOR_ORG_ID = "ORG-L1";
export const DEFAULT_DOWNLINE_ACTOR_USER_ID = "U-ADMIN";

/** 与 23a 发布契约对齐：草稿模板 T-DRAFT-1 / ORG-L1 / U-ADMIN */
export const DEFAULT_PACKAGE_TEMPLATE_ID = "T-DRAFT-1";
export const DEFAULT_PUBLISH_ACTOR_ORG_ID = "ORG-L1";
export const DEFAULT_PUBLISH_ACTOR_USER_ID = "U-ADMIN";

/** 与 24a 覆盖契约对齐：已发布 T-PUB-1 / ORG-L2 / price=2800 */
export const DEFAULT_OVERRIDE_TEMPLATE_ID = "T-PUB-1";
export const DEFAULT_OVERRIDE_ACTOR_ORG_ID = "ORG-L2";
export const DEFAULT_OVERRIDE_ACTOR_USER_ID = "U-SZ";
export const DEFAULT_OVERRIDE_ID = "OV-1";
export const DEFAULT_OVERRIDE_PRICE_CENTS = 2800;

/** 派生下一版本：源 T-PUB-1（已发布）/ 新草稿 T-NEXT-1 / ORG-L1 */
export const DEFAULT_NEXT_VERSION_SOURCE_ID = "T-PUB-1";
export const DEFAULT_NEXT_VERSION_NEW_ID = "T-NEXT-1";
export const DEFAULT_NEXT_VERSION_DISPLAY_NAME = "30天卡 v2";
export const DEFAULT_NEXT_VERSION_PRICE_CENTS = 3200;
export const DEFAULT_NEXT_VERSION_DURATION_DAYS = 30;

const TIMEOUT_MS = 8000;

/** POST /admin/onboarding/{applicationId}/approve 成功读模型（AC-40 · 26a 对齐） */
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
 * POST /admin/onboarding/{applicationId}/approve
 * body `{ shopName }`；错误经 fetchJson 已拼 suggestion。
 */
export async function postApproveOnboarding(
  req: ApproveOnboardingRequest,
): Promise<ApproveOnboardingResult> {
  const applicationId =
    req.applicationId.trim() || DEFAULT_ONBOARDING_APPLICATION_ID;
  const shopName = req.shopName.trim() || DEFAULT_SHOP_NAME;
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/admin/onboarding/${encodeURIComponent(applicationId)}/approve`,
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

/** POST /operator/onboarding/{applicationId}/approve-downline 成功读模型（29a） */
export type ApproveOperatorDownlineResult = {
  orgId: string;
  name: string;
  parentOrgId: string | null;
  operatorCapability: boolean;
  status: string;
};

export type ApproveOperatorDownlineRequest = {
  applicationId: string;
  actorUserId: string;
  actorOrgId: string;
};

/**
 * POST /operator/onboarding/{applicationId}/approve-downline
 * body `{ actorUserId, actorOrgId }`；错误经 fetchJson 已拼 suggestion。
 */
export async function postApproveOperatorDownline(
  req: ApproveOperatorDownlineRequest,
): Promise<ApproveOperatorDownlineResult> {
  const applicationId =
    req.applicationId.trim() || DEFAULT_DOWNLINE_APPLICATION_ID;
  const actorUserId =
    req.actorUserId.trim() || DEFAULT_DOWNLINE_ACTOR_USER_ID;
  const actorOrgId = req.actorOrgId.trim() || DEFAULT_DOWNLINE_ACTOR_ORG_ID;
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/operator/onboarding/${encodeURIComponent(applicationId)}/approve-downline`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ actorUserId, actorOrgId }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseApproveOperatorDownline(raw);
}

function parseApproveOperatorDownline(
  raw: Record<string, unknown>,
): ApproveOperatorDownlineResult {
  const orgId = String(raw.orgId ?? raw.id ?? "");
  const name = String(raw.name ?? "");
  const parentRaw = raw.parentOrgId ?? raw.parentId;
  const parentOrgId =
    parentRaw == null || parentRaw === "" ? null : String(parentRaw);
  const operatorCapability = Boolean(
    raw.operatorCapability ?? raw.hasOperatorCapability ?? false,
  );
  const status = String(raw.status ?? "");
  if (!orgId) {
    throw new Error("批下线响应缺少 orgId");
  }
  return { orgId, name, parentOrgId, operatorCapability, status };
}

/** GET /operator/templates/{id} 读模型 */
export type PackageTemplateDto = {
  templateId: string;
  ownerOrgId: string;
  version: number;
  status: string;
  inheritedFrom: string | null;
};

/**
 * GET /operator/templates/{templateId}
 */
export async function fetchPackageTemplate(
  templateId: string = DEFAULT_PACKAGE_TEMPLATE_ID,
): Promise<PackageTemplateDto> {
  const id = templateId.trim() || DEFAULT_PACKAGE_TEMPLATE_ID;
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/operator/templates/${encodeURIComponent(id)}`,
    { timeoutMs: TIMEOUT_MS },
  );
  const versionRaw = raw.version;
  const version =
    typeof versionRaw === "number"
      ? versionRaw
      : Number.parseInt(String(versionRaw ?? ""), 10);
  const inheritedRaw = raw.inheritedFrom;
  return {
    templateId: String(raw.templateId ?? raw.id ?? id),
    ownerOrgId: String(raw.ownerOrgId ?? ""),
    version: Number.isFinite(version) ? version : 0,
    status: String(raw.status ?? ""),
    inheritedFrom:
      inheritedRaw == null || inheritedRaw === ""
        ? null
        : String(inheritedRaw),
  };
}

export type OrganizationDto = {
  id: string;
  name: string;
  parentId: string | null;
  status: string;
  operatorCapability: boolean;
};

/**
 * GET /operator/orgs/{orgId}
 */
export async function fetchOrganization(
  orgId: string,
): Promise<OrganizationDto> {
  const id = orgId.trim();
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/operator/orgs/${encodeURIComponent(id)}`,
    { timeoutMs: TIMEOUT_MS },
  );
  const parentRaw = raw.parentId;
  return {
    id: String(raw.id ?? id),
    name: String(raw.name ?? ""),
    parentId:
      parentRaw == null || parentRaw === "" ? null : String(parentRaw),
    status: String(raw.status ?? ""),
    operatorCapability: Boolean(raw.operatorCapability),
  };
}

export type OnboardingApplicationDto = {
  id: string;
  orgId: string;
  capability: string;
  status: string;
};

/**
 * GET /operator/onboarding/{applicationId}
 */
export async function fetchOnboardingApplication(
  applicationId: string,
): Promise<OnboardingApplicationDto> {
  const id = applicationId.trim();
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/operator/onboarding/${encodeURIComponent(id)}`,
    { timeoutMs: TIMEOUT_MS },
  );
  return {
    id: String(raw.id ?? id),
    orgId: String(raw.orgId ?? ""),
    capability: String(raw.capability ?? ""),
    status: String(raw.status ?? ""),
  };
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
  const base = apiBase();
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

/** POST /operator/templates/{id}/next-version 成功读模型（AC-25） */
export type NextVersionDraftResult = {
  templateId: string;
  ownerOrgId: string;
  version: number;
  status: string;
  inheritedFrom: string | null;
};

export type NextVersionDraftRequest = {
  sourceTemplateId: string;
  newTemplateId: string;
  actorUserId: string;
  actorOrgId: string;
  displayName: string;
  priceCents: number;
  durationDays: number;
};

/**
 * POST /operator/templates/{sourceTemplateId}/next-version
 */
export async function postCreateNextVersionDraft(
  req: NextVersionDraftRequest,
): Promise<NextVersionDraftResult> {
  const sourceTemplateId =
    req.sourceTemplateId.trim() || DEFAULT_NEXT_VERSION_SOURCE_ID;
  const newTemplateId =
    req.newTemplateId.trim() || DEFAULT_NEXT_VERSION_NEW_ID;
  const actorUserId =
    req.actorUserId.trim() || DEFAULT_PUBLISH_ACTOR_USER_ID;
  const actorOrgId = req.actorOrgId.trim() || DEFAULT_PUBLISH_ACTOR_ORG_ID;
  const displayName =
    req.displayName.trim() || DEFAULT_NEXT_VERSION_DISPLAY_NAME;
  const priceCents = Number.isFinite(req.priceCents)
    ? req.priceCents
    : DEFAULT_NEXT_VERSION_PRICE_CENTS;
  const durationDays = Number.isFinite(req.durationDays)
    ? req.durationDays
    : DEFAULT_NEXT_VERSION_DURATION_DAYS;
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/operator/templates/${encodeURIComponent(sourceTemplateId)}/next-version`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        actorUserId,
        actorOrgId,
        newTemplateId,
        displayName,
        priceCents,
        durationDays,
      }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  const templateId = String(raw.templateId ?? raw.id ?? newTemplateId);
  const ownerOrgId = String(raw.ownerOrgId ?? "");
  const versionRaw = raw.version;
  const version =
    typeof versionRaw === "number"
      ? versionRaw
      : Number.parseInt(String(versionRaw ?? ""), 10);
  const status = String(raw.status ?? "");
  const inheritedRaw = raw.inheritedFrom;
  const inheritedFrom =
    inheritedRaw == null || inheritedRaw === ""
      ? null
      : String(inheritedRaw);
  if (!templateId || !status || !Number.isFinite(version)) {
    throw new Error("派生下一版本响应缺少 templateId/status/version");
  }
  return { templateId, ownerOrgId, version, status, inheritedFrom };
}

/** POST /operator/templates/{templateId}/overrides 成功读模型（AC-26） */
export type ActivatePackageOverrideResult = {
  overrideId: string;
  orgId: string;
  templateId: string;
  templateVersion: number;
  status: string;
  priceCents: number | null;
};

export type ActivatePackageOverrideRequest = {
  templateId: string;
  actorUserId: string;
  actorOrgId: string;
  overrideId: string;
  patches: { price?: number; displayName?: string };
};

/**
 * POST /operator/templates/{templateId}/overrides
 * body `{ actorUserId, actorOrgId, overrideId, patches }`；错误经 fetchJson 已拼 suggestion。
 */
export async function postActivatePackageOverride(
  req: ActivatePackageOverrideRequest,
): Promise<ActivatePackageOverrideResult> {
  const templateId = req.templateId.trim() || DEFAULT_OVERRIDE_TEMPLATE_ID;
  const actorUserId =
    req.actorUserId.trim() || DEFAULT_OVERRIDE_ACTOR_USER_ID;
  const actorOrgId = req.actorOrgId.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID;
  const overrideId = req.overrideId.trim() || DEFAULT_OVERRIDE_ID;
  const patches: Record<string, string | number> = {};
  if (req.patches.price != null && Number.isFinite(req.patches.price)) {
    patches.price = req.patches.price;
  }
  if (
    req.patches.displayName != null &&
    req.patches.displayName.trim().length > 0
  ) {
    patches.displayName = req.patches.displayName.trim();
  }
  if (Object.keys(patches).length === 0) {
    patches.price = DEFAULT_OVERRIDE_PRICE_CENTS;
  }
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/operator/templates/${encodeURIComponent(templateId)}/overrides`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        actorUserId,
        actorOrgId,
        overrideId,
        patches,
      }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseActivatePackageOverride(raw, overrideId, templateId);
}

function parseActivatePackageOverride(
  raw: Record<string, unknown>,
  fallbackOverrideId: string,
  fallbackTemplateId: string,
): ActivatePackageOverrideResult {
  const overrideId = String(raw.overrideId ?? raw.id ?? fallbackOverrideId);
  const orgId = String(raw.orgId ?? "");
  const templateId = String(raw.templateId ?? fallbackTemplateId);
  const versionRaw = raw.templateVersion ?? raw.version;
  const templateVersion =
    typeof versionRaw === "number"
      ? versionRaw
      : Number.parseInt(String(versionRaw ?? ""), 10);
  const status = String(raw.status ?? "");
  const patches =
    typeof raw.patches === "object" && raw.patches != null
      ? (raw.patches as Record<string, unknown>)
      : null;
  const priceRaw = raw.priceCents ?? raw.price ?? patches?.price;
  const priceCents =
    priceRaw == null || priceRaw === ""
      ? null
      : typeof priceRaw === "number"
        ? priceRaw
        : Number.parseInt(String(priceRaw), 10);
  if (!overrideId || !status) {
    throw new Error("激活覆盖响应缺少 overrideId/status");
  }
  return {
    overrideId,
    orgId,
    templateId,
    templateVersion: Number.isFinite(templateVersion) ? templateVersion : 0,
    status,
    priceCents:
      priceCents != null && Number.isFinite(priceCents) ? priceCents : null,
  };
}

/** POST /operator/overrides/{overrideId}/revoke 成功读模型（AC-31 · 30a） */
export type RevokePackageOverrideResult = {
  overrideId: string;
  orgId: string;
  templateId: string;
  status: string;
  priceCents: number | null;
  displayName: string | null;
};

export type RevokePackageOverrideRequest = {
  overrideId: string;
  actorUserId: string;
  actorOrgId: string;
};

/**
 * POST /operator/overrides/{overrideId}/revoke
 * body `{ actorUserId, actorOrgId }`；错误经 fetchJson 已拼 suggestion。
 * 对齐 RevokePackageOverrideHttpIT：默认 OV-1 / ORG-L2 / U-SZ。
 */
export async function postRevokePackageOverride(
  req: RevokePackageOverrideRequest,
): Promise<RevokePackageOverrideResult> {
  const overrideId = req.overrideId.trim() || DEFAULT_OVERRIDE_ID;
  const actorUserId =
    req.actorUserId.trim() || DEFAULT_OVERRIDE_ACTOR_USER_ID;
  const actorOrgId = req.actorOrgId.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID;
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/operator/overrides/${encodeURIComponent(overrideId)}/revoke`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ actorUserId, actorOrgId }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseRevokePackageOverride(raw, overrideId);
}

function parseRevokePackageOverride(
  raw: Record<string, unknown>,
  fallbackOverrideId: string,
): RevokePackageOverrideResult {
  const overrideId = String(raw.overrideId ?? raw.id ?? fallbackOverrideId);
  const orgId = String(raw.orgId ?? "");
  const templateId = String(raw.templateId ?? "");
  const status = String(raw.status ?? "");
  const priceRaw = raw.priceCents ?? raw.price;
  const priceCents =
    priceRaw == null || priceRaw === ""
      ? null
      : typeof priceRaw === "number"
        ? priceRaw
        : Number.parseInt(String(priceRaw), 10);
  const displayRaw = raw.displayName;
  const displayName =
    displayRaw == null || displayRaw === "" ? null : String(displayRaw);
  if (!overrideId || !status) {
    throw new Error("撤销覆盖响应缺少 overrideId/status");
  }
  return {
    overrideId,
    orgId,
    templateId,
    status,
    priceCents:
      priceCents != null && Number.isFinite(priceCents) ? priceCents : null,
    displayName,
  };
}

/** GET /operator/orgs/{orgId}/templates/{templateId}/effective-product 读模型（AC-26） */
export type EffectiveProductResult = {
  templateId: string;
  templateVersion: number;
  overrideId: string | null;
  priceCents: number;
  displayName: string;
  durationDays: number;
};

export type GetEffectiveProductRequest = {
  orgId: string;
  templateId: string;
};

/**
 * GET /operator/orgs/{orgId}/templates/{templateId}/effective-product
 * 错误经 fetchJson 已拼 suggestion。
 */
export async function getEffectiveProduct(
  req: GetEffectiveProductRequest,
): Promise<EffectiveProductResult> {
  const orgId = req.orgId.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID;
  const templateId = req.templateId.trim() || DEFAULT_OVERRIDE_TEMPLATE_ID;
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/operator/orgs/${encodeURIComponent(orgId)}/templates/${encodeURIComponent(templateId)}/effective-product`,
    {
      method: "GET",
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseEffectiveProduct(raw, templateId);
}

function parseEffectiveProduct(
  raw: Record<string, unknown>,
  fallbackTemplateId: string,
): EffectiveProductResult {
  const templateId = String(raw.templateId ?? fallbackTemplateId);
  const versionRaw = raw.templateVersion ?? raw.version;
  const templateVersion =
    typeof versionRaw === "number"
      ? versionRaw
      : Number.parseInt(String(versionRaw ?? ""), 10);
  const overrideRaw = raw.overrideId;
  const overrideId =
    overrideRaw == null || overrideRaw === ""
      ? null
      : String(overrideRaw);
  const product =
    typeof raw.product === "object" && raw.product != null
      ? (raw.product as Record<string, unknown>)
      : null;
  const priceRaw = raw.priceCents ?? raw.price ?? product?.priceCents;
  const priceCents =
    typeof priceRaw === "number"
      ? priceRaw
      : Number.parseInt(String(priceRaw ?? ""), 10);
  const displayName = String(
    raw.displayName ?? product?.displayName ?? "",
  );
  const durationRaw = raw.durationDays ?? product?.durationDays;
  const durationDays =
    typeof durationRaw === "number"
      ? durationRaw
      : Number.parseInt(String(durationRaw ?? ""), 10);
  if (!templateId || !Number.isFinite(priceCents)) {
    throw new Error("有效商品响应缺少 templateId/priceCents");
  }
  return {
    templateId,
    templateVersion: Number.isFinite(templateVersion) ? templateVersion : 0,
    overrideId,
    priceCents,
    displayName,
    durationDays: Number.isFinite(durationDays) ? durationDays : 0,
  };
}

