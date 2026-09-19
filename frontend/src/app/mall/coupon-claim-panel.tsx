"use client";

/**
 * 商城领券客户端岛 — 默认 CAMP-OK / U1 / T-C1；成功展示券 id/status。
 */

import { FormEvent, useState } from "react";
import {
  parseUserCouponStatus,
  toUserCouponView,
  type UserCouponView,
} from "@/domains/mall/domain/user-coupon-view";
import {
  DEFAULT_MALL_CAMPAIGN,
  DEFAULT_MALL_TEMPLATE,
  DEFAULT_MALL_USER,
  postClaimCoupon,
} from "@/domains/mall/infrastructure/mall-gateway";
import styles from "./page.module.css";

export function CouponClaimPanel() {
  const [campaignId, setCampaignId] = useState(DEFAULT_MALL_CAMPAIGN);
  const [userId, setUserId] = useState(DEFAULT_MALL_USER);
  const [templateId, setTemplateId] = useState(DEFAULT_MALL_TEMPLATE);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<UserCouponView | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setView(null);
    try {
      const r = await postClaimCoupon({
        campaignId: campaignId.trim() || DEFAULT_MALL_CAMPAIGN,
        userId: userId.trim() || DEFAULT_MALL_USER,
        templateId: templateId.trim() || DEFAULT_MALL_TEMPLATE,
      });
      setView(
        toUserCouponView({
          id: r.id,
          userId: r.userId,
          templateId: r.templateId,
          status: parseUserCouponStatus(r.status),
        }),
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>活动领券</h2>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          campaignId
          <input
            value={campaignId}
            onChange={(e) => setCampaignId(e.target.value)}
          />
        </label>
        <label>
          userId
          <input value={userId} onChange={(e) => setUserId(e.target.value)} />
        </label>
        <label>
          templateId
          <input
            value={templateId}
            onChange={(e) => setTemplateId(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "领取中…" : "领券"}
        </button>
      </form>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      {view ? <ClaimResultView view={view} /> : null}
    </section>
  );
}

function ClaimResultView({ view }: { view: UserCouponView }) {
  return (
    <dl className={styles.dl}>
      <dt>券 id</dt>
      <dd>{view.id}</dd>
      <dt>用户</dt>
      <dd>{view.userId}</dd>
      <dt>模板</dt>
      <dd>{view.templateId}</dd>
      <dt>状态</dt>
      <dd>
        <span className={`${styles.badge} ${styles.badgeAvailable}`}>
          {view.status}
        </span>
      </dd>
      <dt>结账可选</dt>
      <dd>{view.checkoutSelectable ? "是" : "否"}</dd>
      {view.blockMessage ? (
        <>
          <dt>说明</dt>
          <dd>{view.blockMessage}</dd>
        </>
      ) : null}
    </dl>
  );
}
