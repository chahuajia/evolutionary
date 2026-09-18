/**
 * 商城 BC 防腐层 — HTTP gateway（DTO → 前端读模型）
 * 浏览器经 Next `/api` rewrite；RSC 直连 BACKEND_ORIGIN。
 */

import { fetchJson } from "@/shared/http/fetch-json";

export const DEFAULT_MALL_USER = "U1";
export const DEFAULT_MALL_CAMPAIGN = "CAMP-OK";
export const DEFAULT_MALL_TEMPLATE = "T-C1";

const TIMEOUT_MS = 8000;

function resolveMallApiBase(): string {
  if (typeof window === "undefined") {
    return process.env.BACKEND_ORIGIN ?? "http://localhost:8080";
  }
  return process.env.NEXT_PUBLIC_API_BASE ?? "/api";
}

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
  const base = resolveMallApiBase();
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
  const status = String(raw.status ?? "");
  if (!id || !status) {
    throw new Error("领券响应缺少 id/status");
  }
  return { id, userId, templateId, status };
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
  const base = resolveMallApiBase();
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
  const base = resolveMallApiBase();
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

/** POST /mall/orders/checkout-with-coupons 成功读模型（AC-42+ · 切片16b） */
export type CheckoutWithCouponsResult = PurchaseMallOrderResult;

export type CheckoutWithCouponsRequest = {
  userId: string;
  merchantOrgId?: string;
  skuId?: string;
  qty?: number;
  /** 用户券 id；可空（等价无券结账） */
  userCouponIds?: string[];
};

/**
 * POST /mall/orders/checkout-with-coupons — 余额购 SKU（可带券核销）
 */
export async function postCheckoutWithCoupons(
  req: CheckoutWithCouponsRequest,
): Promise<CheckoutWithCouponsResult> {
  const base = resolveMallApiBase();
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
        userCouponIds: req.userCouponIds ?? [],
      }),
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parsePurchase(raw);
}
