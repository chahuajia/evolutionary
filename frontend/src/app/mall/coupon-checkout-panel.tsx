"use client";

/**
 * 带券结账客户端岛 — 先领券再 POST checkout-with-coupons（AC-42..44）。
 */

import { FormEvent, useState } from "react";
import { toCheckoutView } from "@/domains/mall/domain/mall-checkout-view";
import {
  canSelectCouponForCheckout,
  parseUserCouponStatus,
  toUserCouponView,
  type UserCouponView,
} from "@/domains/mall/domain/user-coupon-view";
import {
  DEFAULT_MALL_CAMPAIGN,
  DEFAULT_MALL_MERCHANT,
  DEFAULT_MALL_SKU,
  DEFAULT_MALL_TEMPLATE,
  DEFAULT_MALL_USER,
  postCheckoutWithCoupons,
  postClaimCoupon,
} from "@/domains/mall/infrastructure/mall-gateway";
import styles from "./page.module.css";

export function CouponCheckoutPanel() {
  const [userId, setUserId] = useState(DEFAULT_MALL_USER);
  const [couponId, setCouponId] = useState("");
  const [couponView, setCouponView] = useState<UserCouponView | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ReturnType<
    typeof toCheckoutView
  > | null>(null);

  async function claimFirst() {
    setBusy(true);
    setError(null);
    try {
      const c = await postClaimCoupon({
        campaignId: DEFAULT_MALL_CAMPAIGN,
        userId: userId.trim() || DEFAULT_MALL_USER,
        templateId: DEFAULT_MALL_TEMPLATE,
      });
      const view = toUserCouponView({
        id: c.id,
        userId: c.userId,
        templateId: c.templateId,
        status: parseUserCouponStatus(c.status),
      });
      setCouponId(c.id);
      setCouponView(view);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (
      couponView &&
      couponId.trim() === couponView.id &&
      !canSelectCouponForCheckout(couponView.status)
    ) {
      setError(couponView.blockMessage ?? "当前券不可用于结账");
      return;
    }
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const ids = couponId.trim() ? [couponId.trim()] : [];
      const r = await postCheckoutWithCoupons({
        userId: userId.trim() || DEFAULT_MALL_USER,
        merchantOrgId: DEFAULT_MALL_MERCHANT,
        skuId: DEFAULT_MALL_SKU,
        qty: 1,
        userCouponIds: ids,
      });
      setResult(toCheckoutView(r));
      if (couponView && ids[0] === couponView.id) {
        setCouponView(
          toUserCouponView({
            ...couponView,
            status: "USED",
          }),
        );
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  const checkoutBlocked =
    couponView != null &&
    couponId.trim() === couponView.id &&
    !couponView.checkoutSelectable;

  return (
    <section className={styles.panel}>
      <h2>带券结账（HTTP · AC-42..44）</h2>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          userId
          <input value={userId} onChange={(e) => setUserId(e.target.value)} />
        </label>
        <label>
          userCouponId
          <input
            value={couponId}
            onChange={(e) => {
              setCouponId(e.target.value);
              if (!couponView || e.target.value.trim() !== couponView.id) {
                setCouponView(null);
              }
            }}
            placeholder="先点领券或粘贴券 id"
          />
        </label>
        <div style={{ display: "flex", gap: "0.5rem" }}>
          <button type="button" disabled={busy} onClick={claimFirst}>
            先领券
          </button>
          <button type="submit" disabled={busy || checkoutBlocked}>
            {busy ? "结账中…" : "带券结账"}
          </button>
        </div>
      </form>
      {checkoutBlocked && couponView?.blockMessage ? (
        <p className={styles.note} role="status">
          {couponView.blockMessage}
        </p>
      ) : null}
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {result ? (
        <p>
          {result.orderId} · {result.status} · 实付 ¥{result.paidAmountYuan} ·
          优惠 ¥{result.discountYuan}
        </p>
      ) : null}
    </section>
  );
}
