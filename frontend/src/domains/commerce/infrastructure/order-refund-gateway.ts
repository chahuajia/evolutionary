/**
 * 订单退款防腐 — POST /commerce/orders/{orderId}/refund
 * RSC 直连 Spring；浏览器经 Next `/api` rewrite。
 */

import { fetchJson } from "@/shared/http/fetch-json";

const TIMEOUT_MS = 8000;

function resolveApiBase(): string {
  if (typeof window === "undefined") {
    return process.env.BACKEND_ORIGIN ?? "http://localhost:8080";
  }
  return process.env.NEXT_PUBLIC_API_BASE ?? "/api";
}

/** POST /commerce/orders/{orderId}/refund 成功读模型（对齐 RefundResult） */
export type RefundOrderResult = {
  orderId: string;
  status: string;
  entitlementId?: string;
  entitlementStatus?: string;
  refundedAt?: string;
};

/**
 * POST /commerce/orders/{orderId}/refund — 无 body；错误经 fetchJson 已拼 suggestion。
 */
export async function postRefundOrder(
  orderId: string,
): Promise<RefundOrderResult> {
  const id = orderId.trim();
  if (!id) throw new Error("orderId required");
  const base = resolveApiBase();
  const raw = await fetchJson<Record<string, unknown>>(
    `${base}/commerce/orders/${encodeURIComponent(id)}/refund`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      timeoutMs: TIMEOUT_MS,
    },
  );
  return parseRefundResult(raw, id);
}

function parseRefundResult(
  raw: Record<string, unknown>,
  fallbackOrderId: string,
): RefundOrderResult {
  const order =
    raw.order != null && typeof raw.order === "object"
      ? (raw.order as Record<string, unknown>)
      : null;
  const entitlement =
    raw.entitlement != null && typeof raw.entitlement === "object"
      ? (raw.entitlement as Record<string, unknown>)
      : null;

  const orderId = String(
    raw.orderId ?? order?.id ?? fallbackOrderId,
  );
  const status = String(
    raw.status ?? order?.status ?? raw.orderStatus ?? "",
  );
  if (!orderId || !status) {
    throw new Error("退款响应缺少 orderId/status");
  }

  const entitlementIdRaw =
    raw.entitlementId ?? entitlement?.id;
  const entitlementStatusRaw =
    raw.entitlementStatus ?? entitlement?.status;
  const refundedAtRaw = raw.refundedAt ?? order?.refundedAt;

  return {
    orderId,
    status,
    entitlementId:
      entitlementIdRaw != null ? String(entitlementIdRaw) : undefined,
    entitlementStatus:
      entitlementStatusRaw != null
        ? String(entitlementStatusRaw)
        : undefined,
    refundedAt:
      refundedAtRaw != null ? String(refundedAtRaw) : undefined,
  };
}
