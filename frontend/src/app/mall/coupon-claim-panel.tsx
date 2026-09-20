"use client";

/**
 * 商城领券客户端岛 — 默认 CAMP-OK / U1 / T-C1；成功展示券 id/status。
 * GET Campaign + CouponTemplate 对齐 ACTIVE / budgetRemaining / faceBudget。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import type { CampaignView } from "@/domains/mall/domain/campaign-view";
import {
  parseUserCouponStatus,
  toUserCouponView,
  type UserCouponView,
} from "@/domains/mall/domain/user-coupon-view";
import { loadCampaign } from "@/domains/mall/application/load-campaign";
import { loadCouponTemplate } from "@/domains/mall/application/load-coupon-template";
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
  const [campaign, setCampaign] = useState<CampaignView | null>(null);
  const [faceCents, setFaceCents] = useState<number | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<UserCouponView | null>(null);

  useEffect(() => {
    let cancelled = false;
    const tid = templateId.trim() || DEFAULT_MALL_TEMPLATE;
    setLoadError(null);
    loadCouponTemplate(tid)
      .then((tpl) => {
        if (cancelled) return;
        setFaceCents(tpl.faceBudgetCents);
      })
      .catch((err) => {
        if (cancelled) return;
        setFaceCents(null);
        setLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [templateId]);

  useEffect(() => {
    if (faceCents == null) {
      setCampaign(null);
      return;
    }
    let cancelled = false;
    const id = campaignId.trim() || DEFAULT_MALL_CAMPAIGN;
    setLoadError(null);
    loadCampaign(id, faceCents)
      .then((view) => {
        if (cancelled) return;
        setCampaign(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setCampaign(null);
        setLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [campaignId, faceCents]);

  const gate = useMemo(() => {
    if (faceCents == null || !campaign) {
      return {
        claimAllowed: false,
        blockMessage: loadError ?? "正在加载活动/模板…",
      };
    }
    return {
      claimAllowed: campaign.claimAllowed,
      blockMessage: campaign.blockMessage,
    };
  }, [campaign, faceCents, loadError]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!gate.claimAllowed || faceCents == null) {
      setError(gate.blockMessage ?? "活动不可领券");
      return;
    }
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
      const dto = await fetchCampaign(
        campaignId.trim() || DEFAULT_MALL_CAMPAIGN,
      );
      setCampaign(
        toCampaignView(
          {
            id: dto.id,
            ownerOrgId: dto.ownerOrgId,
            name: dto.name,
            budgetRemainingCents: dto.budgetRemainingCents,
            status: parseCampaignStatus(dto.status),
          },
          faceCents,
        ),
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
      {campaign && faceCents != null ? (
        <p className={styles.note} role="status">
          {campaign.id} · {campaign.name} · {campaign.status} · 预算余{" "}
          {campaign.budgetRemainingCents}¢ · 面额 {faceCents}¢
          {campaign.claimAllowed ? " · 可领券" : ""}
        </p>
      ) : null}
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
        <button type="submit" disabled={busy || !gate.claimAllowed}>
          {busy ? "领取中…" : "领券"}
        </button>
      </form>

      {!gate.claimAllowed && gate.blockMessage ? (
        <p className={styles.note} role="status">
          {gate.blockMessage}
        </p>
      ) : null}

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
