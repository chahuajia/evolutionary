"use client";

/**
 * 带券结账客户端岛 — 可先领券再 POST /mall/orders/checkout-with-coupons（切片16b）。
 * 默认 U1 / M1 / S1；成功展示 paidAmountCents。
 */

import { FormEvent, useState } from "react";
import {
  DEFAULT_MALL_CAMPAIGN,
  DEFAULT_MALL_MERCHANT,
  DEFAULT_MALL_SKU,
  DEFAULT_MALL_TEMPLATE,
  DEFAULT_MALL_USER,
  postCheckoutWithCoupons,
  postClaimCoupon,
  type CheckoutWithCouponsResult,
  type ClaimCouponResult,
} from "@/domains/mall/infrastructure/mall-gateway";
import styles from "./page.module.css";

function parseCouponIds(raw: string): string[] {
  return raw
    .split(/[,，\s]+/)
    .map((s) => s.trim())
    .filter(Boolean);
}

export function CouponCheckoutPanel() {
  const [userId, setUserId] = useState(DEFAULT_MALL_USER);
  const [merchantOrgId, setMerchantOrgId] = useState(DEFAULT_MALL_MERCHANT);
  const [skuId, setSkuId] = useState(DEFAULT_MALL_SKU);
  const [qty, setQty] = useState(1);
  const [campaignId, setCampaignId] = useState(DEFAULT_MALL_CAMPAIGN);
  const [templateId, setTemplateId] = useState(DEFAULT_MALL_TEMPLATE);
  const [userCouponIdsText, setUserCouponIdsText] = useState("");
  const [claimBusy, setClaimBusy] = useState(false);
  const [checkoutBusy, setCheckoutBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [claim, setClaim] = useState<ClaimCouponResult | null>(null);
  const [result, setResult] = useState<CheckoutWithCouponsResult | null>(null);

  async function onClaim(e: FormEvent) {
    e.preventDefault();
    setClaimBusy(true);
    setError(null);
    setClaim(null);
    try {
      const r = await postClaimCoupon({
        campaignId: campaignId.trim() || DEFAULT_MALL_CAMPAIGN,
        userId: userId.trim() || DEFAULT_MALL_USER,
        templateId: templateId.trim() || DEFAULT_MALL_TEMPLATE,
      });
      setClaim(r);
      setUserCouponIdsText((prev) => {
        const ids = parseCouponIds(prev);
        if (ids.includes(r.id)) return prev;
        return ids.length ? `${prev.trim()},${r.id}` : r.id;
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setClaimBusy(false);
    }
  }

  async function onCheckout(e: FormEvent) {
    e.preventDefault();
    setCheckoutBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await postCheckoutWithCoupons({
        userId: userId.trim() || DEFAULT_MALL_USER,
        merchantOrgId: merchantOrgId.trim() || DEFAULT_MALL_MERCHANT,
        skuId: skuId.trim() || DEFAULT_MALL_SKU,
        qty,
        userCouponIds: parseCouponIds(userCouponIdsText),
      });
      setResult(r);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setCheckoutBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>带券结账（HTTP · 切片16b）</h2>

      <form className={styles.form} onSubmit={onClaim}>
        <label>
          campaignId（可选先领券）
          <input
            value={campaignId}
            onChange={(e) => setCampaignId(e.target.value)}
          />
        </label>
        <label>
          templateId
          <input
            value={templateId}
            onChange={(e) => setTemplateId(e.target.value)}
          />
        </label>
        <button type="submit" disabled={claimBusy || checkoutBusy}>
          {claimBusy ? "领取中…" : "先领券"}
        </button>
      </form>

      {claim ? (
        <p>
          已领券 {claim.id} · {claim.status}
        </p>
      ) : null}

      <form className={styles.form} onSubmit={onCheckout}>
        <label>
          userId
          <input value={userId} onChange={(e) => setUserId(e.target.value)} />
        </label>
        <label>
          merchantOrgId
          <input
            value={merchantOrgId}
            onChange={(e) => setMerchantOrgId(e.target.value)}
          />
        </label>
        <label>
          skuId
          <input value={skuId} onChange={(e) => setSkuId(e.target.value)} />
        </label>
        <label>
          qty
          <input
            type="number"
            min={1}
            value={qty}
            onChange={(e) => setQty(Number(e.target.value) || 1)}
          />
        </label>
        <label>
          userCouponIds（逗号分隔）
          <input
            value={userCouponIdsText}
            onChange={(e) => setUserCouponIdsText(e.target.value)}
            placeholder="领券后自动填入"
          />
        </label>
        <button type="submit" disabled={checkoutBusy || claimBusy}>
          {checkoutBusy ? "结账中…" : "带券结账"}
        </button>
      </form>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      {result ? (
        <dl className={styles.dl}>
          <dt>订单</dt>
          <dd>{result.orderId}</dd>
          <dt>状态</dt>
          <dd>{result.status}</dd>
          <dt>paidAmountCents</dt>
          <dd>{result.paidAmountCents}</dd>
          <dt>SKU</dt>
          <dd>
            {result.skuId}×{result.qty}
          </dd>
        </dl>
      ) : null}
    </section>
  );
}
