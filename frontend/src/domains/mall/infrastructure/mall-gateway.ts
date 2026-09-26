/**
 * 商城 BC 防腐层 — HTTP gateway（DTO → 前端读模型）
 * 浏览器经 Next `/api` rewrite；RSC 直连 BACKEND_ORIGIN。
 */

import { apiBase } from "@/shared/http/api-base";
import { fetchJson } from "@/shared/http/fetch-json";

export const DEFAULT_MALL_USER = "U1";
export const DEFAULT_MALL_CAMPAIGN = "CAMP-OK";
export const DEFAULT_MALL_TEMPLATE = "T-C1";

const TIMEOUT_MS = 8000;

/** POST /mall/campaigns/{campaignId}/claims 成功读模型 */
export type ClaimCouponResult = {
  id: string;
  userId: string;
  templateId: string;
  status: string;
};

export type ClaimCouponRequest = {
  campaignId?: string;
  userId: string;
  templateId: string;
};

/**
 * POST /mall/campaigns/{campaignId}/claims
 * body `{ userId, templateId }`；错误经 fetchJson 已拼 suggestion。
 */
export async function postClaimCoupon(
  req: ClaimCouponRequest,
): Promise<ClaimCouponResult> {
  const campaignId = req.campaignId ?? DEFAULT_MALL_CAMPAIGN;
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/mall/campaigns/${encodeURIComponent(campaignId)}/claims`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        userId: req.userId,
        templateId: req.templateId,
      }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseClaimCoupon(raw);
}

function parseClaimCoupon(raw: Record<string, unknown>): ClaimCouponResult {
  const id = String(raw.id ?? "");
  const userId = String(raw.userId ?? "");
  const templateId = String(raw.templateId ?? "");
  // BE UserCouponView 历史契约为小写；视图层契约为大写枚举
  const status = String(raw.status ?? "").toUpperCase();
  if (!id || !status) {
    throw new Error("领券响应缺少 id/status");
  }
  return { id, userId, templateId, status };
}

/** GET /mall/user-coupons/{id} 读模型（status 已规范化为大写） */
export type UserCouponDto = {
  id: string;
  userId: string;
  templateId: string;
  status: string;
};

/** GET /mall/user-coupons/{id} — 对齐 checkoutSelectable */
export async function fetchUserCoupon(userCouponId: string): Promise<UserCouponDto> {
  const base = apiBase();
  const id = userCouponId.trim();
  if (!id) {
    throw new Error("userCouponId required");
  }
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/mall/user-coupons/${encodeURIComponent(id)}`,
    { method: "GET", timeoutMs: TIMEOUT_MS },
  );
  return parseClaimCoupon(raw);
}

export const DEFAULT_MALL_SKU = "S1";
export const DEFAULT_MALL_MERCHANT = "M1";

/** POST /mall/orders 成功读模型（AC-41） */
export type PurchaseMallOrderResult = {
  orderId: string;
  userId: string;
  merchantOrgId: string;
  status: string;
  paidAmountCents: number;
  skuId: string;
  qty: number;
};

export type PurchaseMallOrderRequest = {
  userId: string;
  merchantOrgId?: string;
  skuId?: string;
  qty?: number;
};

/**
 * POST /mall/orders — 余额购 SKU
 */
export async function postPurchaseMallOrder(
  req: PurchaseMallOrderRequest,
): Promise<PurchaseMallOrderResult> {
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(`${base}/mall/orders`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      userId: req.userId,
      merchantOrgId: req.merchantOrgId ?? DEFAULT_MALL_MERCHANT,
      skuId: req.skuId ?? DEFAULT_MALL_SKU,
      qty: req.qty ?? 1,
    }),
    timeoutMs: TIMEOUT_MS,
  });
  return parsePurchase(raw);
}

function parsePurchase(raw: Record<string, unknown>): PurchaseMallOrderResult {
  const orderId = String(raw.orderId ?? "");
  const status = String(raw.status ?? "");
  if (!orderId || !status) {
    throw new Error("下单响应缺少 orderId/status");
  }
  return {
    orderId,
    userId: String(raw.userId ?? ""),
    merchantOrgId: String(raw.merchantOrgId ?? ""),
    status,
    paidAmountCents: Number(raw.paidAmountCents ?? 0),
    skuId: String(raw.skuId ?? ""),
    qty: Number(raw.qty ?? 0),
  };
}

export type CheckoutWithCouponsRequest = {
  userId: string;
  merchantOrgId?: string;
  skuId?: string;
  qty?: number;
  userCouponIds: string[];
};

export type CheckoutWithCouponsResult = PurchaseMallOrderResult & {
  discountCents: number;
};

/**
 * POST /mall/orders/checkout-with-coupons
 */
export async function postCheckoutWithCoupons(
  req: CheckoutWithCouponsRequest,
): Promise<CheckoutWithCouponsResult> {
  const base = apiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/mall/orders/checkout-with-coupons`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        userId: req.userId,
        merchantOrgId: req.merchantOrgId ?? DEFAULT_MALL_MERCHANT,
        skuId: req.skuId ?? DEFAULT_MALL_SKU,
        qty: req.qty ?? 1,
        userCouponIds: req.userCouponIds,
      }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  const baseResult = parsePurchase(raw);
  return {
    ...baseResult,
    discountCents: Number(raw.discountCents ?? 0),
  };
}

/** GET /mall/skus/{skuId} 读模型 */
export type MallSkuDto = {
  id: string;
  merchantOrgId: string;
  name: string;
  priceCents: number;
  stock: number;
  status: string;
};

/** GET /mall/skus/{skuId} */
export async function fetchMallSku(
  skuId: string = DEFAULT_MALL_SKU,
): Promise<MallSkuDto> {
  const base = apiBase();
  const id = skuId.trim() || DEFAULT_MALL_SKU;
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/mall/skus/${encodeURIComponent(id)}`,
    { method: "GET", timeoutMs: TIMEOUT_MS },
  );
  return {
    id: String(raw.id ?? id),
    merchantOrgId: String(raw.merchantOrgId ?? ""),
    name: String(raw.name ?? ""),
    priceCents: Number(raw.priceCents ?? 0),
    stock: Number(raw.stock ?? 0),
    status: String(raw.status ?? ""),
  };
}

/** GET /mall/campaigns/{campaignId} 读模型 */
export type CampaignDto = {
  id: string;
  ownerOrgId: string;
  name: string;
  budgetTotalCents: number;
  budgetRemainingCents: number;
  status: string;
  couponTemplateIds: string[];
};

/** GET /mall/campaigns/{campaignId} */
export async function fetchCampaign(
  campaignId: string = DEFAULT_MALL_CAMPAIGN,
): Promise<CampaignDto> {
  const base = apiBase();
  const id = campaignId.trim() || DEFAULT_MALL_CAMPAIGN;
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/mall/campaigns/${encodeURIComponent(id)}`,
    { method: "GET", timeoutMs: TIMEOUT_MS },
  );
  const templates = Array.isArray(raw.couponTemplateIds)
    ? raw.couponTemplateIds.map((t) => String(t))
    : [];
  return {
    id: String(raw.id ?? id),
    ownerOrgId: String(raw.ownerOrgId ?? ""),
    name: String(raw.name ?? ""),
    budgetTotalCents: Number(raw.budgetTotalCents ?? 0),
    budgetRemainingCents: Number(raw.budgetRemainingCents ?? 0),
    status: String(raw.status ?? ""),
    couponTemplateIds: templates,
  };
}

/** GET /mall/merchants/{orgId} 读模型 */
export type MerchantProfileDto = {
  orgId: string;
  shopName: string;
  status: string;
};

/** GET /mall/merchants/{orgId} */
export async function fetchMerchantProfile(
  orgId: string = DEFAULT_MALL_MERCHANT,
): Promise<MerchantProfileDto> {
  const base = apiBase();
  const id = orgId.trim() || DEFAULT_MALL_MERCHANT;
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/mall/merchants/${encodeURIComponent(id)}`,
    { method: "GET", timeoutMs: TIMEOUT_MS },
  );
  return {
    orgId: String(raw.orgId ?? id),
    shopName: String(raw.shopName ?? ""),
    status: String(raw.status ?? ""),
  };
}

/** GET /mall/coupon-templates/{templateId} 读模型 */
export type CouponTemplateDto = {
  id: string;
  issuerOrgId: string;
  kind: string;
  value: number;
  faceBudgetCents: number;
  campaignId: string;
  mutexGroup: string;
};

/** GET /mall/coupon-templates/{templateId} */
export async function fetchCouponTemplate(
  templateId: string = DEFAULT_MALL_TEMPLATE,
): Promise<CouponTemplateDto> {
  const base = apiBase();
  const id = templateId.trim() || DEFAULT_MALL_TEMPLATE;
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/mall/coupon-templates/${encodeURIComponent(id)}`,
    { method: "GET", timeoutMs: TIMEOUT_MS },
  );
  return {
    id: String(raw.id ?? id),
    issuerOrgId: String(raw.issuerOrgId ?? ""),
    kind: String(raw.kind ?? ""),
    value: Number(raw.value ?? 0),
    faceBudgetCents: Number(raw.faceBudgetCents ?? raw.value ?? 0),
    campaignId: String(raw.campaignId ?? ""),
    mutexGroup: String(raw.mutexGroup ?? ""),
  };
}
