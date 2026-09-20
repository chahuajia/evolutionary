"use client";

/**
 * 批准商家入驻客户端岛 — POST /admin/onboarding/{id}/approve（AC-40 · 26a）。
 * GET /operator/onboarding/{id} 对齐 approveAllowed（仅 SUBMITTED）。
 */

import { FormEvent, useEffect, useState } from "react";
import {
  DEFAULT_ONBOARDING_APPLICATION_ID,
  DEFAULT_SHOP_NAME,
  loadOnboardingApplication,
} from "@/domains/operator/application/load-onboarding-application";
import type { OnboardingApplicationView } from "@/domains/operator/domain/onboarding-application-view";
import type { MerchantProfileView } from "@/domains/mall/domain/merchant-profile-view";
import { runApproveOnboarding } from "@/domains/operator/application/run-approve-onboarding";
import styles from "./page.module.css";

export function OnboardingApprovePanel() {
  const [applicationId, setApplicationId] = useState(
    DEFAULT_ONBOARDING_APPLICATION_ID,
  );
  const [shopName, setShopName] = useState(DEFAULT_SHOP_NAME);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [appView, setAppView] = useState<OnboardingApplicationView | null>(
    null,
  );
  const [appLoadError, setAppLoadError] = useState<string | null>(null);
  const [merchant, setMerchant] = useState<MerchantProfileView | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = applicationId.trim() || DEFAULT_ONBOARDING_APPLICATION_ID;
    setAppLoadError(null);
    loadOnboardingApplication(id)
      .then((view) => {
        if (cancelled) return;
        setAppView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setAppView(null);
        setAppLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [applicationId]);

  const approveAllowed = appView != null && appView.approveAllowed;
  const blockMessage =
    appView == null
      ? (appLoadError ?? "正在加载入驻申请…")
      : appView.blockMessage;

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!approveAllowed) {
      setError(blockMessage ?? "当前状态不可批准");
      return;
    }
    setBusy(true);
    setError(null);
    setMerchant(null);
    try {
      const id = applicationId.trim() || DEFAULT_ONBOARDING_APPLICATION_ID;
      setMerchant(
        await runApproveOnboarding({
          applicationId: id,
          shopName: shopName.trim() || DEFAULT_SHOP_NAME,
        }),
      );
      setAppView(await loadOnboardingApplication(id));
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>平台批准商家入驻（HTTP · AC-40）</h2>
      <p className={styles.note} role="status">
        GET 申请对齐仅 SUBMITTED 可批
        {appView ? ` · ${appView.id}=${appView.statusLabel}` : ""}
        {appView?.approveAllowed ? " · 可批准" : ""}
      </p>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          applicationId
          <input
            value={applicationId}
            onChange={(e) => setApplicationId(e.target.value)}
          />
        </label>
        <label>
          shopName
          <input
            value={shopName}
            onChange={(e) => setShopName(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy || !approveAllowed}>
          {busy ? "批准中…" : "批准入驻"}
        </button>
      </form>
      {!approveAllowed && blockMessage ? (
        <p className={styles.note} role="status">
          {blockMessage}
        </p>
      ) : null}
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {merchant ? (
        <p>
          商家 {merchant.orgId} · {merchant.shopName} · {merchant.statusLabel}
          {merchant.tradeAllowed ? " · 可交易" : ""}
        </p>
      ) : null}
    </section>
  );
}
