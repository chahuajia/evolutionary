"use client";

/**
 * 批准商家入驻客户端岛 — POST /admin/onboarding/{id}/approve（AC-40 · 26a）。
 */

import { FormEvent, useMemo, useState } from "react";
import {
  toOnboardingApplicationView,
  type OnboardingStatus,
} from "@/domains/operator/domain/onboarding-application-view";
import {
  parseMerchantProfileStatus,
  toMerchantProfileView,
  type MerchantProfileView,
} from "@/domains/mall/domain/merchant-profile-view";
import {
  DEFAULT_ONBOARDING_APPLICATION_ID,
  DEFAULT_SHOP_NAME,
  postApproveOnboarding,
} from "@/domains/operator/infrastructure/operator-gateway";
import styles from "./page.module.css";

/** 种子 APP-M1 为 SUBMITTED；批准成功后本地记 APPROVED 禁再批。 */
const SEED_APP = {
  id: DEFAULT_ONBOARDING_APPLICATION_ID,
  orgId: "ORG-NEW",
  capability: "MERCHANT",
  status: "SUBMITTED" as OnboardingStatus,
};

export function OnboardingApprovePanel() {
  const [applicationId, setApplicationId] = useState(
    DEFAULT_ONBOARDING_APPLICATION_ID,
  );
  const [shopName, setShopName] = useState(DEFAULT_SHOP_NAME);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [localStatus, setLocalStatus] = useState<OnboardingStatus>(
    SEED_APP.status,
  );
  const [merchant, setMerchant] = useState<MerchantProfileView | null>(null);

  const appView = useMemo(() => {
    const isSeed =
      (applicationId.trim() || DEFAULT_ONBOARDING_APPLICATION_ID) ===
      DEFAULT_ONBOARDING_APPLICATION_ID;
    if (!isSeed) {
      return {
        approveAllowed: true,
        blockMessage: null as string | null,
        statusLabel: "非种子申请 · 由后端判态",
      };
    }
    const view = toOnboardingApplicationView({
      ...SEED_APP,
      status: localStatus,
    });
    return view;
  }, [applicationId, localStatus]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!appView.approveAllowed) {
      setError(appView.blockMessage ?? "当前状态不可批准");
      return;
    }
    setBusy(true);
    setError(null);
    setMerchant(null);
    try {
      const r = await postApproveOnboarding({
        applicationId:
          applicationId.trim() || DEFAULT_ONBOARDING_APPLICATION_ID,
        shopName: shopName.trim() || DEFAULT_SHOP_NAME,
      });
      setMerchant(
        toMerchantProfileView({
          orgId: r.orgId,
          shopName: r.shopName,
          status: parseMerchantProfileStatus(r.status),
        }),
      );
      if (
        (applicationId.trim() || DEFAULT_ONBOARDING_APPLICATION_ID) ===
        DEFAULT_ONBOARDING_APPLICATION_ID
      ) {
        setLocalStatus("APPROVED");
      }
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
        {appView.statusLabel}
        {appView.approveAllowed ? " · 可批准" : ""}
      </p>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          applicationId
          <input
            value={applicationId}
            onChange={(e) => {
              setApplicationId(e.target.value);
              setLocalStatus(SEED_APP.status);
            }}
          />
        </label>
        <label>
          shopName
          <input
            value={shopName}
            onChange={(e) => setShopName(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy || !appView.approveAllowed}>
          {busy ? "批准中…" : "批准入驻"}
        </button>
      </form>
      {!appView.approveAllowed && appView.blockMessage ? (
        <p className={styles.note} role="status">
          {appView.blockMessage}
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
